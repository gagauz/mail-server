package ru.gagauz.mail.server;

import ru.gagauz.mail.client.ClientConnection;
import ru.gagauz.socket.server.ConnectionFactory;
import ru.gagauz.socket.server.IOConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class Server extends Thread {

    private static final Logger log = Logger.getLogger("Server");

    private static final ConnectionFactory connetcionFactory = new IOConnectionFactory();
    private static final Map<Integer, ServerConnectionThread> openConnections = new ConcurrentHashMap<Integer, ServerConnectionThread>();
    private ServerConnection serverConnection;
    private final int port;
    private final boolean secured;

    public Server(int port, boolean secured) {
        this.port = port;
        this.secured = secured;
        setDaemon(true);
    }

    public void stopServer() {
        if (null != serverConnection) {
            for (ServerConnectionThread node : new ArrayList<ServerConnectionThread>(openConnections.values())) {
                node.closeSocket();
            }
            try {
                serverConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            interrupt();
        }
    }

    public void startServer() {
        try {
            serverConnection = secured ? connetcionFactory.createSecureServer(port) : connetcionFactory.createServer(port);
            String addr = serverConnection.getAddress();
            log.info("**** Server started on " + ":" + port + " ****");
        } catch (Exception e) {
            throw new RuntimeException("Failed to start server", e);
        }
        start();
    }

    @Override
    public void run() {
        try {

            while (!isInterrupted()) {
                log.info("**** Waiting for clients... ****");
                ClientConnection connection = serverConnection.accept();
                createConnectionThread(connection);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    protected void createConnectionThread(ClientConnection socket) throws IOException {
        ServerConnectionThread connectionThread = new ServerConnectionThread(this, socket.getSocket());
        System.out.println("*** Connection with " + socket.getSocket().getRemoteSocketAddress() + " was estabilished.");
        connectionThread.setCommandProcessor(getCommandProcessor());
        openConnections.put(connectionThread.hashCode(), connectionThread);
    }

    protected abstract CommandProcessor getCommandProcessor();

    public ConnectionFactory getConnectionFactory() {
        return connetcionFactory;
    }

    protected void removeServerThread(ServerConnectionThread serverConnectionThread) {
        System.out.println("*** Connection with  " + serverConnectionThread.getSocket().socket.getRemoteSocketAddress() + " was closed.");
        openConnections.remove(serverConnectionThread.hashCode());
    }
}
