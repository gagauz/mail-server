package ru.miga.mail.smtp;

import ru.miga.mail.db.DB;
import ru.miga.mail.db.FileSysDB;
import ru.miga.mail.server.CommandProcessor;
import ru.miga.mail.server.Server;

import java.io.File;

public class SmtpServer extends Server {

    public SmtpServer(int port) {
        super(port);
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
