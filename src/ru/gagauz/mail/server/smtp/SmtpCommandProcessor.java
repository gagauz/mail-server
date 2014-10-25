package ru.gagauz.mail.server.smtp;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.exception.BadSequenceException;
import ru.gagauz.mail.exception.InvalidArgumentException;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.ServerConnectionThread;
import ru.gagauz.socket.server.SocketIO;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmtpCommandProcessor extends CommandProcessor {

    private static Pattern R_EMAIL = Pattern.compile("([a-z0-9\\.\\-_]+@[a-z0-9\\.-]+)", Pattern.CASE_INSENSITIVE);

    private static Pattern R_CODE = Pattern.compile("([0-9]+)");

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
    private final SmtpServer server;
    private Mail mail;

    private class Mail {
        String mailFrom;
        List<String> rcptTo;
        StringBuffer body = new StringBuffer();

        void addRcptTo(String rcptTo) {
            if (null == this.rcptTo) {
                this.rcptTo = new ArrayList<String>();
            }
            this.rcptTo.add(rcptTo);
        }
    }

    public SmtpCommandProcessor(SmtpServer server, DB db) {
        this.server = server;
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
        try {
            if ("HELO".equals(c[0])) {
                send("250 %s says ALOHA!", server.getDomains()[0]);
            } else if ("EHLO".equals(c[0])) {
                send("250-%s says ALOHA!", server.getDomains()[0]);
                send("250-SIZE 14680064");
                send("250-PIPELINING");
                send("250 HELP");
                mail = null;
            } else if (command.startsWith("MAIL FROM:")) {
                String[] names = validateAndSplitEmail(c2[1]);
                if (server.isLocal(names[1])) {
                    if (database.getMailBox(names[0]) == null) {
                        send("550 Unknown sender " + c2[1]);
                        return;
                    }
                }
                mail = new Mail();
                mail.mailFrom = c2[1];
                send("250 OK");
            } else if (command.startsWith("RCPT TO:")) {
                if (null == mail || null == mail.mailFrom) {
                    throw new BadSequenceException();
                }
                String[] names = validateAndSplitEmail(c2[1]);
                if (server.isLocal(names[1])) {
                    if (database.getMailBox(names[0]) == null) {
                        send("550 Unknown recipient " + c2[1]);
                        return;
                    }
                    send("250 OK");
                    mail.addRcptTo(c2[1]);
                    return;
                }
                send("251 OK");
                mail.addRcptTo(c2[1]);
            } else if ("DATA".equals(c[0])) {
                if (mail.rcptTo == null) {
                    throw new BadSequenceException();
                }
                send("354 Ready");
                System.out.println("+ Starting reading data...");
                while (true) {
                    String data = socketIO.readString();
                    System.out.println(data);
                    if (null == data || data.endsWith("\r\n.\r\n")) {
                        break;
                    } else {
                        mail.body.append(data);
                    }
                }
                System.out.println("+ Finished reading data...");

                try {
                    deliver(mail);
                } catch (Exception e) {
                    send("550 " + e.getMessage());
                    return;
                }
                send("250 OK");
                return;
            } else if ("QUIT".equals(c[0])) {
                mail = null;
                send("221 Aloha");
                socketIO.closeSocket();
            } else {
                send("500 Syntax error " + command);
            }
        } catch (BadSequenceException e) {
            send("503 Bad sequence of commands " + command);
        } catch (InvalidArgumentException e) {
            send("501 Syntax error in arguments " + command);
        }

    }

    @Override
    public void hello() {
        String hostname = server.getDomains()[0];
        socketIO.writeString("220 " + hostname + " ESMTP Gagauz mail-server is glad to see you!");
    }

    @Override
    public void quit() {
        socketIO.closeSocket();
    }

    private String[] validateAndSplitEmail(String addr) {
        String email = extractFirstEmail(addr);
        if (null == email) {
            throw new InvalidArgumentException();
        }
        String[] names = email.split("@");
        if (names.length != 2) {
            throw new InvalidArgumentException();
        }
        return names;
    }

    private String extractFirstEmail(String address) {
        Matcher m = R_EMAIL.matcher(address);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private int extractCode(String response) {
        Matcher m = R_CODE.matcher(response);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private boolean deliver(Mail mail) {
        try {
            for (String addr : mail.rcptTo) {
                String email = extractFirstEmail(addr);
                String[] names = email.split("@");
                if (server.isLocal(names[1])) {
                    deliverLocal(mail, names[0], names[1]);
                } else {
                    deliverOther(mail, names[0], names[1]);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void deliverLocal(Mail mail, String name, String host) {
        System.out.println("Deliver local " + name + " " + host);
        database.getMailBox(name).deliver(mail.body);
    }

    private void deliverOther(Mail mail, String name, String host) {
        System.out.println("Deliver other " + name + " " + host);
        int[] ports = new int[] {25, 587};
        for (int port : ports) {
            SocketIO socket = tryToGetConnection(host, port);
            if (socket != null) {
                socketIO.writeString("EHLO");
                waitForAnswer(socketIO, 25);
                socketIO.writeString("MAIL FROM:" + mail.mailFrom);
                waitForAnswer(socketIO, 25);
                for (String rcptTo : mail.rcptTo) {
                    socketIO.writeString("RCPT TO:" + rcptTo);
                    waitForAnswer(socketIO, 25);
                }
                socketIO.writeString("DATA");
                waitForAnswer(socketIO, 35);
                socketIO.writeString(mail.body.toString());
                socketIO.writeString(".");
                waitForAnswer(socketIO, 2);
                socketIO.writeString("QUIT");
                waitForAnswer(socketIO, 22);
                return;
            }
        }
        throw new IllegalStateException("Failed to deliver mail to " + name + "@" + host);
    }

    private void waitForAnswer(SocketIO socket, int code) {
        String string = socketIO.readString();
        int resCode = extractCode(string);
        if (code > 100 && code != resCode) {
            throw new IllegalStateException("The answer code is " + resCode + " but not " + code);
        } else if (code > 10 && code < 100 && code != resCode / 10) {
            throw new IllegalStateException("The answer code is " + resCode + " but not " + code + "*");
        } else if (code > 1 && code < 10 && code != resCode / 100) {
            throw new IllegalStateException("The answer code is " + resCode + " but not " + code + "**");
        }
    }

    private SocketIO tryToGetConnection(String host, int port) {
        try {
            Socket socket = server.getConnectionFactory().createClient(host, 25).getSocket();
            SocketIO socketIO = new SocketIO(socket);
            waitForAnswer(socketIO, 22);
            return socketIO;
        } catch (Exception e) {
            return null;
        }
    }
}
