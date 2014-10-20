package ru.gagauz.mail.server.pop;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.db.MailBox;
import ru.gagauz.mail.db.Message;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.ServerConnectionThread;
import ru.gagauz.utils.hash.HashUtils;

public class Pop3CommandProcessor extends CommandProcessor {

    public static enum Command {
        AUTH(false, 0),
        CAPA(false, 0),
        APOP(false, 2),
        USER(false, 1),
        PASS(false, 1),
        DELE(true, 1),
        LIST(true, 0),
        NOOP(true, 0),
        RETR(true, 1),
        RSET(true, 0),
        STAT(true, 0),
        TOP(true, 2),
        QUIT(false, 0),
        UIDL(true, 0);

        private final boolean requireLogin;
        private final int minArgs;

        Command(boolean requireLogin, int minArgs) {
            this.requireLogin = requireLogin;
            this.minArgs = minArgs;
        }

        public boolean requireLogin() {
            return requireLogin;
        }

        public boolean valid(String[] commandAndArgs) {
            return commandAndArgs.length >= minArgs + 1;
        }
    }

    private final DB database;
    private MailBox tmpMailBox;
    private MailBox mailBox;
    private String timestamp;

    public Pop3CommandProcessor(DB db) {
        this.database = db;
    }

    @Override
    public void initConnection(ServerConnectionThread serverConnectionThread) {
        this.socketIO = serverConnectionThread.getSocket();
        this.mailBox = null;
        hello();
    }

    @Override
    public void hello() {
        timestamp = String.format("<%d.%d@%s>", Thread.currentThread().getId(), System.currentTimeMillis() / 1000, socketIO.socket.getLocalAddress());
        send("+OK POP3 server ready %s", timestamp);
    }

    @Override
    public void quit() {
        socketIO.closeSocket();
    }

    @Override
    public void process(String command) {
        System.out.println(">" + command);
        String[] args = command.split("[ ]+");
        Command com = null;
        try {
            com = Command.valueOf(args[0].toUpperCase());
        } catch (Exception e) {
            send("-ERR Never heard of command " + args[0]);
            return;
        }

        if (!com.valid(args)) {
            send("-ERR Invalid number of arguments for command %s (%s)", args[0], command);
            return;
        }

        if (com.requireLogin() && null == mailBox) {
            send("-ERR Invalid number of arguments for command %s (%s)", args[0], command);
            return;
        }

        try {
            switch (com) {

            case AUTH: {
                send("+ " + HashUtils.encodeBase64("OK".getBytes()));
                return;
            }

            case APOP: {
                mailBox = database.getMailBox(args[1]);
                if (null == mailBox) {
                    send("-ERR never heard of mailbox %s", args[1]);
                    return;
                }
                if (args[2].equals(HashUtils.md5(timestamp + mailBox.getPass()))) {
                    send("+OK maildrop locked and ready");
                } else {
                    send("-ERR invalid password");
                }
                return;
            }

            case CAPA: {
                send("+OK List of capabilities follows");
                //                send("STSL");
                send("SASL PLAIN ANONYMOUS");
                send("TOP");
                send("USER");
                send("UIDL");
                send("IMPLEMENTATION Miga own POP3 server");
                send(".");
                return;
            }

            case USER: {
                tmpMailBox = database.getMailBox(args[1]);
                send("+OK");
                return;
            }

            case PASS: {
                if (null != tmpMailBox && args[1].equals(tmpMailBox.getPass())) {
                    mailBox = tmpMailBox;
                    tmpMailBox = null;
                    send("+OK Mailbox open, %d messages", mailBox.getCount());
                } else {
                    send("-ERR Invalid name or password");
                }
                return;
            }

            case NOOP: {
                send("+OK");
                return;
            }

            case QUIT: {
                send("+OK");
                quit();
                return;
            }

            case DELE: {
                int index = Integer.parseInt(args[1]);
                Message message = mailBox.getMessages()[index - 1];
                message.trash();
                send("+OK Message %d was moved to trash", index);
                return;
            }

            case UIDL: {
                if (args.length == 1) {
                    send("+OK");
                    for (int i = 0; i < mailBox.getMessages().length; i++) {
                        send("%d %s", i + 1, mailBox.getMessages()[i].getUid());
                    }
                    send(".");
                } else {
                    int index = Integer.parseInt(args[1]);
                    Message message = mailBox.getMessages()[index - 1];
                    send("+OK %d %s", index, message.getUid());
                }
                return;
            }

            case LIST: {
                if (args.length == 1) {
                    send("+OK %d messages (%d octets)", mailBox.getCount(), mailBox.getSize());
                    for (int i = 0; i < mailBox.getMessages().length; i++) {
                        send("%d %d", i + 1, mailBox.getMessages()[i].getSize());
                    }
                    send(".");
                } else {
                    int index = Integer.parseInt(args[1]);
                    Message message = mailBox.getMessages()[index - 1];
                    send("+OK %d %d ...", index, message.getSize());
                }
                return;
            }

            case TOP: {
                try {
                    int index = Integer.parseInt(args[1]);
                    int lines = Integer.parseInt(args[2]);
                    Message message = mailBox.getMessages()[index - 1];
                    send("+OK");
                    send(message.getContent().getHeader());
                    send("");
                    String body = message.getContent().getBody();
                    String[] strings = body.split("\n", lines + 1);
                    for (int i = 0; i < strings.length && i < lines; i++) {
                        send(strings[i]);
                    }
                    send(".");
                } catch (ArrayIndexOutOfBoundsException e) {
                    send("-ERR no message #", args[1]);
                } catch (NumberFormatException e) {
                    send("-ERR invalid number %s", args[1]);
                }
                return;
            }

            case RETR: {
                int index = Integer.parseInt(args[1]);
                Message message = mailBox.getMessages()[index - 1];
                send("+OK %d octets", message.getSize());
                send(message.getContent().getContent());
                send(".");
                return;
            }

            case RSET: {
                for (int i = 0; i < mailBox.getMessages().length; i++) {
                    mailBox.getMessages()[i].untrash();
                }
                send("+OK");
                return;
            }

            case STAT: {
                send("+OK %d %d", mailBox.getCount(), mailBox.getSize());
                return;
            }

            }

        } catch (ArrayIndexOutOfBoundsException e) {
            send("-ERR no message %s, only %d messages in maildrop", args[1], mailBox.getMessages().length);
        } catch (NumberFormatException e) {
            send("-ERR Expecting number but %s was given", args[1]);
        }

    }

    public static void main(String[] args) {
        //"e888fab2c8faa397fe4a4f6fb627c285";
        //"e888fab2c8faa397fe4a4f6fb627c285";
        System.out.println(HashUtils.md5("<15.1413814557@/127.0.0.1>123123"));
    }
}
