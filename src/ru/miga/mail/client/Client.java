package ru.miga.mail.client;

import ru.miga.socket.server.ConnectionFactory;
import ru.miga.socket.server.IOConnectionFactory;
import ru.miga.socket.server.SocketIO;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Client extends SocketIO {

    private static final Logger log = Logger.getLogger("Client");

    private static final ConnectionFactory connectionFactory = new IOConnectionFactory();

    protected Set<Long> lockWaitingQueue = new HashSet<Long>();

    public Client() throws IOException {
        this("0.0.0.0", 110);
    }

    public Client(String addr, int port) throws IOException {
        super(connect(addr, port));
    }

    private static Socket connect(String host, int port) {
        log.info("Connecting to " + host + ":" + port + "...");
        Socket socket = null;
        while (true) {
            try {
                socket = connectionFactory.createClient(host, port).getSocket();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.severe("Can't connetct to " + host + ":" + port + ". Trying to reconnect...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
        return socket;
    }

    public static void main(String[] args) {
        try {
            Client c = new Client();
            System.out.println("Reading hello");
            String hello = c.readString();
            System.out.println(hello);
            System.out.println("Sending LIST ");
            c.writeString("LIST");
            System.out.println("Reading response");
            String s = c.readString();
            System.out.println(s);

            System.out.println("Sending QUIT");
            c.writeString("QUIT");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
