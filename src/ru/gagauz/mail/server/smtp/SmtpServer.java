package ru.gagauz.mail.server.smtp;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.db.FileSysDB;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.Server;
import ru.gagauz.utils.collection.Tools;

import java.io.File;

public class SmtpServer extends Server {

    private final String[] servedDomains;

    public SmtpServer(int port, String... domains) {
        super(port, false);
        this.servedDomains = domains;
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        DB db = new FileSysDB(new File("/mailbox"));
        return new SmtpCommandProcessor(this, db);
    }

    public boolean isLocal(String host) {
        return Tools.contains(servedDomains, host);
    }

    public String[] getDomains() {
        return servedDomains;
    }

    public static void main(String[] args) {
        Server server = new SmtpServer(25, "ivaga.com");
        server.startServer();
        while (true) {

        }
    }

}
