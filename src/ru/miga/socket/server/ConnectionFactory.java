package ru.miga.socket.server;

import ru.miga.mail.server.ServerConnection;

import ru.miga.mail.client.ClientConnection;

import java.io.IOException;

public interface ConnectionFactory {
    ServerConnection createServer(int port) throws IOException;

	ClientConnection createClient(String host, int port) throws IOException;
}
