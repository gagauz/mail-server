package ru.miga.mail.pop;

import ru.miga.mail.db.DB;
import ru.miga.mail.db.MailBox;
import ru.miga.mail.db.Message;
import ru.miga.mail.server.CommandProcessor;
import ru.miga.mail.server.ServerConnectionThread;
import ru.miga.socket.server.SocketIO;
import ru.miga.utils.HashUtils;

public class Pop3CommandProcessor implements CommandProcessor {

    private final DB database;
    private SocketIO socketIO;
    private MailBox mailBox;

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
        socketIO.writeString("+OK POP3 server ready <ololo>");
    }

    @Override
    public void quit() {
        socketIO.closeSocket();
    }

    @Override
    public void process(String command) {
        System.out.println(">" + command);
        String[] c = command.split("[ ]+");
        if ("APOP".equals(c[0]) && c.length > 2) {
            mailBox = database.getMailBox(c[1]);
            if (null == mailBox) {
                send("-ERR never heard of mailbox %s", c[1]);
                return;
            }
            if (HashUtils.md5(c[2]).equals(mailBox.getPass())) {
                send("+OK maildrop locked and ready");
            } else {
                send("-ERR invalid password");
            }
            return;
        } else if ("CAPA".equals(c[0])) {
            send("+OK List of capabilities follows");
            send("SASL PLAIN ANONYMOUS");
            send("TOP");
            send("USER");
            send("UIDL");
            send("IMPLEMENTATION Miga own POP3 server");
            send(".");
            return;
        } else if ("USER".equals(c[0])) {
            mailBox = database.getMailBox(c[1]);
            if (null != mailBox) {
                send("+OK " + c[1] + " is a valid mailbox");
            } else {
                send("-ERR never heard of mailbox %s", c[1]);
            }
            return;
        } else if ("NOOP".equals(c[0])) {
            send("+OK");
            return;
        } else if ("QUIT".equals(c[0])) {
            send("+OK");
            quit();
            return;
        }
        if (null != mailBox) {
            if ("PASS".equals(c[0])) {
                if (c[1].equals(mailBox.getPass())) {
                    send("+OK Mailbox open, %d messages", mailBox.getCount());
                } else {
                    send("-ERR invalid password");
                }
            } else if ("DELE".equals(c[0])) {
                send("+OK message deleted");
            } else if ("UIDL".equals(c[0])) {
                if (c.length == 1) {
                    send("+OK");
                    for (int i = 0; i < mailBox.getMessages().length; i++) {
                        send("%d %s", i + 1, mailBox.getMessages()[i].getUid());
                    }
                    send(".");
                } else {
                    try {
                        int index = Integer.parseInt(c[1]);
                        Message message = mailBox.getMessages()[index - 1];
                        send("+OK %d %s", index, message.getUid());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        send("-ERR no message %s, only %d messages in maildrop", c[1], mailBox.getMessages().length);
                    } catch (NumberFormatException e) {
                        send("-ERR invalid number %s", c[1]);
                    }
                }

            } else if ("LIST".equals(c[0])) {
                if (c.length == 1) {
                    send("+OK %d messages (%d octets)", mailBox.getCount(), mailBox.getSize());
                    for (int i = 0; i < mailBox.getMessages().length; i++) {
                        send("%d %d", i + 1, mailBox.getMessages()[i].getSize());
                    }
                    send(".");
                } else {

                    try {
                        int index = Integer.parseInt(c[1]);
                        Message message = mailBox.getMessages()[index - 1];
                        send("+OK %d %d ...", index, message.getSize());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        send("-ERR no message %s, only %d messages in maildrop", c[1], mailBox.getMessages().length);
                    } catch (NumberFormatException e) {
                        send("-ERR invalid number %s", c[1]);
                    }
                }

            } else if ("RETR".equals(c[0]) && c.length > 1) {
                try {
                    int index = Integer.parseInt(c[1]);
                    Message message = mailBox.getMessages()[index - 1];
                    send("+OK message follows");
                    send(message.getContent().getContent());
                    send(".");
                } catch (ArrayIndexOutOfBoundsException e) {
                    send("-ERR no message %s, only %d messages in maildrop", c[1], mailBox.getMessages().length);
                } catch (NumberFormatException e) {
                    send("-ERR invalid number %s", c[1]);
                }
            } else if ("RSET".equals(c[0])) {
                send("+OK");
            } else if ("STAT".equals(c[0])) {
                send("+OK %d %d", mailBox.getCount(), mailBox.getSize());
            } else if ("TOP".equals(c[0]) && c.length > 2) {
                try {
                    int index = Integer.parseInt(c[1]);
                    int lines = Integer.parseInt(c[2]);
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
                    send("-ERR no message #", c[1]);
                } catch (NumberFormatException e) {
                    send("-ERR invalid number %s", c[1]);
                }
            } else {
                send("-ERR never heard of command " + command);
            }
        } else {
            send("-ERR never heard of command " + command);
        }

    }

    private void send(String message) {
        socketIO.writeString(message);
    }

    private void send(String message, Object... params) {
        socketIO.writeString(String.format(message, params));
    }
}
