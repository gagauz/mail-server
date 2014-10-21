package ru.gagauz.mail.db;

import ru.gagauz.utils.collection.Tools;

import java.io.File;
import java.util.Map;

public class FileSysDB implements DB {

    private final File directory;
    private final File queue;
    private final Map<String, MailBox> mailBoxCache = Tools.hasMap();

    public FileSysDB(File directory) {
        if (directory.isDirectory()) {
            this.directory = directory;
        } else {
            throw new RuntimeException("Directory " + directory + " does'n exists or is not readable.");
        }
        queue = new File(directory, "sent");
        if (!queue.isDirectory()) {
            queue.mkdir();
        }
    }

    public void storeSent() {

    }

    @Override
    public MailBox getMailBox(String name) {
        MailBox mailBox = mailBoxCache.get(name);
        if (null != mailBox) {
            return mailBox;
        }
        File file = new File(directory.getAbsolutePath() + '/' + name);
        try {
            if (file.isDirectory()) {
                mailBox = new MailBox(file);
                mailBoxCache.put(name, mailBox);
                return mailBox;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
