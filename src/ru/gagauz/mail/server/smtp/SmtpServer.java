package ru.gagauz.mail.server.smtp;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.db.FileSysDB;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.Server;

import java.io.File;

public class SmtpServer extends Server {

    public SmtpServer(int port) {
        super(port, false);
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        DB db = new FileSysDB(new File("r:/mailbox"));
        return new SmtpCommandProcessor(db);
    }

    public static void main(String[] args) {
        Server server = new SmtpServer(25);
        server.startServer();
        while (true) {

        }
    }

}
