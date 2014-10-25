package ru.gagauz.mail.db;

import ru.gagauz.utils.stream.StreamUtils;

import java.io.*;

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

    public void deliver(StringBuffer body) {
        File messageFile = new File(file, System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        OutputStream out;
        try {
            messageFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(messageFile));
            StreamUtils.writeString(out, body.toString());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
