package ru.gagauz.mail.server.pop;

import ru.gagauz.mail.db.DB;
import ru.gagauz.mail.db.FileSysDB;
import ru.gagauz.mail.server.CommandProcessor;
import ru.gagauz.mail.server.Server;

import java.io.File;

public class Pop3Server extends Server {

    public Pop3Server(int port, boolean secure) {
        super(port, secure);
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        DB db = new FileSysDB(new File("/mailbox"));
        return new Pop3CommandProcessor(db);
    }

}
