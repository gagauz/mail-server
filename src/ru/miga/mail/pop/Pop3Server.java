package ru.miga.mail.pop;

import ru.miga.mail.db.DB;
import ru.miga.mail.db.FileSysDB;
import ru.miga.mail.server.CommandProcessor;
import ru.miga.mail.server.Server;

import java.io.File;

public class Pop3Server extends Server {

    public Pop3Server(int port) {
        super(port);
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        DB db = new FileSysDB(new File("r:/mailbox"));
        return new Pop3CommandProcessor(db);
    }

    public static void main(String[] args) {
        Server server = new Pop3Server(110);
        server.startServer();
        while (true) {

        }
    }
}
