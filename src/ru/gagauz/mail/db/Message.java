package ru.gagauz.mail.db;

import ru.gagauz.socket.server.StreamUtils;
import ru.gagauz.utils.hash.HashUtils;

import java.io.File;
import java.io.FileInputStream;

public class Message {

    private final File file;
    private Content content;
    private boolean trash;

    public Message(File file) {
        this.file = file;
    }

    public Content getContent() {
        if (null == content) {
            content = new Content();
        }
        return content;
    }

    public long getSize() {
        return file.length();
    }

    public String getUid() {
        return HashUtils.md5(file.getName());
    }

    public void trash() {
        this.trash = true;
    }

    public void untrash() {
        this.trash = false;
    }

    public class Content {

        String content;
        char NL = '\n';

        Content() {
            try {
                content = StreamUtils.readString(new FileInputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public String getHeader() {
            int nl = content.indexOf("\n\n");
            int nw = content.indexOf("\n\r\n\r");
            int c = content.indexOf("\nContent-Type:");
            if (nl > 0) {
                return content.substring(0, nl);
            }
            if (nw > 0) {
                return content.substring(0, nw);
            }
            return content;
        }

        public String getBody() {
            int nl = content.indexOf("\n\n");
            int nw = content.indexOf("\n\r\n\r");
            int c = content.indexOf("\nContent-Type:");
            if (nl > 0) {
                return content.substring(nl + 2);
            }
            if (nw > 0) {
                return content.substring(nw + 4);
            }
            return content;
        }

        public String getContent() {
            return content;
        }

        public int getSize() {
            return content.length();
        }

    }

}
