package ru.gagauz.socket.server;

import ru.gagauz.mail.client.ClientConnection;
import ru.gagauz.mail.server.ServerConnection;

import java.io.IOException;

public interface ConnectionFactory {
    ServerConnection createServer(int port) throws IOException;

    ClientConnection createClient(String host, int port) throws IOException;

    ServerConnection createSecureServer(int port) throws IOException;

    ClientConnection createSecureClient(String host, int port) throws IOException;
}
