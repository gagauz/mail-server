package ru.gagauz.mail.db;

import ru.gagauz.socket.server.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

public class MailBox {

    private static final FilenameFilter FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(".eml");
        }
    };

    private final File file;
    private Message[] messages;
    private int size;
    private String pass;

    public MailBox(File file) {
        this.file = file;
    }

    public String getPass() {
        if (null != pass) {
            return pass;
        }
        File passFile = new File(file.getAbsolutePath() + "/.passwd");
        try {
            InputStream is = new FileInputStream(passFile);
            pass = StreamUtils.readString(is);
            return pass;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getCount() {
        return getMessages().length;
    }

    public int getSize() {
        getMessages();
        return size;
    }

    public Message[] getMessages() {
        if (null == messages) {
            init();
        }
        return messages;
    }

    private void init() {
        File[] mf = file.listFiles(FILTER);
        messages = new Message[mf.length];
        size = 0;
        for (int i = 0; i < mf.length; i++) {
            messages[i] = new Message(mf[i]);
            size += mf[i].length();
        }
    }
}
