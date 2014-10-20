package ru.gagauz.mail.server.test;

import ru.gagauz.mail.server.Server;
import ru.gagauz.mail.server.pop.Pop3Server;
import ru.gagauz.mail.server.smtp.SmtpServer;

public class TestServers {
    public static void main(String[] args) {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                Server server1 = new Pop3Server(110, false);
                server1.startServer();
            }
        };

        Thread t3 = new Thread() {
            @Override
            public void run() {
                Server server1 = new Pop3Server(995, true);
                server1.startServer();
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                Server server1 = new SmtpServer(25);
                server1.startServer();
            }
        };
        t1.setDaemon(true);
        t1.start();
        t3.setDaemon(true);
        t3.start();
        t2.setDaemon(true);
        t2.start();
        while (true) {

        }
    }
}
