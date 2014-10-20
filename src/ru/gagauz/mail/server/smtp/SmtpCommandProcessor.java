package ru.gagauz.mail.server.smtp;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.ServerConnectionThread;

public class SmtpCommandProcessor extends CommandProcessor {

    public static enum Command {
        HELO(false, 0),
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
    private Mail mail;

    private class Mail {
        String mailFrom;
        String rcptTo;
        StringBuffer body = new StringBuffer();
    }

    public SmtpCommandProcessor(DB db) {
        this.database = db;
    }

    @Override
    public void initConnection(ServerConnectionThread serverConnectionThread) {
        this.socketIO = serverConnectionThread.getSocket();
        hello();
    }

    @Override
    public void process(String command) {
        System.out.println(">" + command);
        if (null == command) {
            return;
        }
        String[] c = command.split("[ ]+");
        String[] c2 = command.split("\\s?:\\s?", 2);
        if ("HELO".equals(c[0]) || "EHLO".equals(c[0])) {
            send("250-smtp2.example.com Hello bob.example.org [192.0.2.201]");
            send("250-SIZE 14680064");
            send("250-PIPELINING");
            send("250 HELP");
            mail = null;
        } else if (command.startsWith("MAIL FROM:")) {
            send("250 " + c2[1] + " sender accepted");
            mail = new Mail();
            mail.mailFrom = c2[1];
        } else if (command.startsWith("RCPT TO:") && mail != null && mail.mailFrom != null) {
            send("250 " + c2[1] + " ok");
            mail.rcptTo = c2[1];
        } else if ("DATA".equals(c[0])) {
            send("354 Enter mail, end with \".\" on a line by itself");
            System.out.println("+ Starting reading data...");
            while (true) {
                String data = socketIO.readString();
                System.out.println(data);
                if (null == data || ".".equals(data.trim())) {
                    break;
                } else {
                    mail.body.append(data);
                }
            }
            System.out.println("+ Finished reading data...");
            send("250 769947 message accepted for delivery");
        } else if ("QUIT".equals(c[0])) {
            send("221 mail.company.tld CommuniGate Pro SMTP closing connection");
            socketIO.closeSocket();
        } else {
            send("550 never heard of command " + command);
        }

    }

    @Override
    public void hello() {
        socketIO.writeString("220 mail.company.tld ESMTP CommuniGate Pro 5.1.4i is glad to see you!");
    }

    @Override
    public void quit() {
        socketIO.closeSocket();
    }

}
