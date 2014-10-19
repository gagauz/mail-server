package ru.miga.mail.smtp;

import ru.miga.mail.server.CommandProcessor;
import ru.miga.mail.server.ServerConnectionThread;

import ru.miga.mail.db.DB;
import ru.miga.mail.db.MailBox;
import ru.miga.socket.server.SocketIO;

public class SmtpCommandProcessor implements CommandProcessor {
    private final DB database;
    private SocketIO socketIO;
    private MailBox mailBox;

    public SmtpCommandProcessor(DB db) {
        this.database = db;
    }

    @Override
    public void initConnection(ServerConnectionThread serverConnectionThread) {
        this.socketIO = serverConnectionThread.getSocket();
        this.mailBox = null;
        hello();
    }

    @Override
    public void process(String command) {
        System.out.println(">" + command);
        if (null == command) {
            return;
        }
        String[] c = command.split("[ ]+");
        if ("HELO".equals(c[0])) {
            send("250 domain name should be qualified");
        } else if (command.startsWith("MAIL FROM:")) {
            send("250 " + c[2] + " sender accepted");
        } else if (command.startsWith("RCPT TO:")) {
            send("250 " + c[2] + " ok");
        } else if ("DATA".equals(c[0])) {
            send("354 Enter mail, end with \".\" on a line by itself");
            System.out.println("+ Starting reading data...");
            while (true) {
                String data = socketIO.readString();
                System.out.println(data);
                if (null == data || ".".equals(data.trim())) {
                    break;
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

    private void send(String message) {
        socketIO.writeString(message);
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
