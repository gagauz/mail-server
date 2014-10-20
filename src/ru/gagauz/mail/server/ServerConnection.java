package ru.gagauz.mail.server;

import ru.gagauz.mail.client.ClientConnection;

import java.io.IOException;

public interface ServerConnection {
    String getAddress() throws IOException;

    void close() throws IOException;

    ClientConnection accept() throws IOException;
}
