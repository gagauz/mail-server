package ru.gagauz.utils.stream;

import java.io.*;

public class StreamUtils {

    private StreamUtils() {
    }

    public static void writeString(OutputStream out, String string) throws IOException {
        out.write(string.getBytes());
        out.flush();
    }

    public static void writeBytes(OutputStream out, byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }

    public static void writeObject(OutputStream out, Object object) throws IOException {
        ObjectOutputStream oi = new ObjectOutputStream(out);
        oi.writeUnshared(object);
        out.flush();
    }

    public static String readString(InputStream in) throws IOException {
        return read(in).toString();
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        return read(in).toByteArray();
    }

    public static Object readObject(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        return ois.readObject();
    }

    public static ByteArrayOutputStream read(InputStream in) throws IOException {
        byte[] bytes = new byte[4094];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int r;
        while (true) {
            r = in.read(bytes);
            if (r == -1) {
                break;
            } else {
                out.write(bytes, 0, r);
                if (r != 4094)
                    break;
            }
        }
        out.flush();
        return out;

    }
}
