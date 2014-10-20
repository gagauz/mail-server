package ru.gagauz.mail.server;

import ru.gagauz.socket.server.SocketIO;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

public class ServerConnectionThread extends Thread {

    protected final Server manager;
    protected final SocketIO socketIO;
    private CommandProcessor commandProcessor;

    public ServerConnectionThread(Server server, Socket socket) throws IOException {
        socketIO = new SocketIO(socket);
        this.manager = server;

        socketIO.onSocketClose(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ServerConnectionThread.this.interrupt();
                manager.removeServerThread(ServerConnectionThread.this);
                return null;
            }
        });
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            String command = socketIO.readString();
            if (null == command) {
                closeSocket();
                break;
            }
            commandProcessor.process(command.trim());
        }
    }

    public Server getManager() {
        return manager;
    }

    public SocketIO getSocket() {
        return socketIO;
    }

    public void closeSocket() {
        this.interrupt();
        socketIO.closeSocket();
    }

    public void setCommandProcessor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
        commandProcessor.initConnection(this);
        start();
    }
}
