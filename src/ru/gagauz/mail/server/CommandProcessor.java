package ru.gagauz.mail.server;

import ru.gagauz.socket.server.SocketIO;

public abstract class CommandProcessor {

    protected SocketIO socketIO;

    public abstract void initConnection(ServerConnectionThread serverConnectionThread);

    public abstract void hello();

    public abstract void quit();

    public abstract void process(String command);

    protected void send(String message) {
        socketIO.writeString(message);
    }

    protected void send(String message, Object... params) {
        socketIO.writeString(String.format(message, params));
    }
}
