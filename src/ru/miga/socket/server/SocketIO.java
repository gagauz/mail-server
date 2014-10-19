package ru.miga.socket.server;

import com.sun.corba.se.pept.transport.ReaderThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class SocketIO {

    private static final Logger log = Logger.getLogger("SocketWrapper");

    public final Socket socket;
    protected ReaderThread readerThread;
    private final InputStream reader;
    protected OutputStream writer;
    protected String lastString;

    private Callable<Void> onSocketClose;

    public SocketIO(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = socket.getInputStream();
        this.writer = socket.getOutputStream();
    }

    public void setLastString(String lastString) {
        this.lastString = lastString;
    }

    public void closeSocket() {
        System.out.println("*********************************************************");
        System.out.println("********** Close socket in SocketWriter " + hashCode() + " ********");
        System.out.println("*********************************************************");
        try {
            if (null != onSocketClose) {
                onSocketClose.call();
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeString(String obj) {
        System.out.println("<" + obj);
        try {
            StreamUtils.writeString(writer, obj + "\n");
        } catch (IOException e) {
            closeSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String readString() {
        try {
            return StreamUtils.readString(reader);
        } catch (IOException e) {
            closeSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public void onSocketClose(Callable<Void> callable) {
        this.onSocketClose = callable;
    }

}
