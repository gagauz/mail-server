package ru.miga.socket.server;

import ru.miga.mail.client.ClientConnection;
import ru.miga.mail.server.ServerConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOConnectionFactory implements ConnectionFactory {

    @Override
    public ClientConnection createClient(final String host, final int port) throws IOException {
        return new ClientConnection() {
            final SocketChannel sChannel;
            {
                sChannel = SocketChannel.open();
                sChannel.configureBlocking(false);
                sChannel.connect(new InetSocketAddress(host, port));
            }

            @Override
            public Socket getSocket() {
                return sChannel.socket();
            }
        };
    }

    @Override
    public ServerConnection createServer(final int port) throws IOException {
        return new ServerConnection() {
            final ServerSocketChannel ssChannel;
            {
                ssChannel = ServerSocketChannel.open();
                ssChannel.configureBlocking(true);
                ssChannel.socket().bind(new InetSocketAddress(port));
            }

            @Override
            public ClientConnection accept() throws IOException {
                final SocketChannel socket = ssChannel.accept();
                return new ClientConnection() {
                    @Override
                    public Socket getSocket() {
                        return socket.socket();
                    }
                };
            }

            @Override
            public String getAddress() {
                return ssChannel.socket().getInetAddress().toString();
            }

            @Override
            public void close() throws IOException {
                ssChannel.close();
            }
        };
    }

}
