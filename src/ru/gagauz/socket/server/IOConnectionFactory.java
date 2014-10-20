package ru.gagauz.socket.server;

import ru.gagauz.mail.client.ClientConnection;
import ru.gagauz.mail.server.ServerConnection;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class IOConnectionFactory implements ConnectionFactory {

    @Override
    public ClientConnection createClient(final String host, final int port) throws IOException {
        return new ClientConnection() {
            final Socket socket = new Socket(InetAddress.getByName(host), port);

            @Override
            public Socket getSocket() {
                return socket;
            }
        };
    }

    @Override
    public ServerConnection createServer(final int port) throws IOException {
        return new ServerConnection() {
            private final ServerSocket server = new ServerSocket(port);

            @Override
            public ClientConnection accept() throws IOException {
                final Socket socket = server.accept();
                return new ClientConnection() {
                    @Override
                    public Socket getSocket() {
                        return socket;
                    }
                };
            }

            @Override
            public String getAddress() {
                return server.getInetAddress().getHostName();
            }

            @Override
            public void close() throws IOException {
                server.close();
            }
        };
    }

    @Override
    public ServerConnection createSecureServer(final int port) throws IOException {
        return new ServerConnection() {
            private final SSLServerSocket server = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);

            {
                server.setEnabledCipherSuites(server.getSupportedCipherSuites());
            }

            @Override
            public ClientConnection accept() throws IOException {

                final Socket socket = server.accept();

                return new ClientConnection() {
                    @Override
                    public Socket getSocket() {
                        return socket;
                    }
                };
            }

            @Override
            public String getAddress() {
                return server.getInetAddress().getHostName();
            }

            @Override
            public void close() throws IOException {
                server.close();
            }
        };
    }

    @Override
    public ClientConnection createSecureClient(final String host, final int port) throws IOException {
        return new ClientConnection() {
            final Socket socket = SSLSocketFactory.getDefault().createSocket(InetAddress.getByName(host), port);

            @Override
            public Socket getSocket() {
                return socket;
            }
        };
    }
}
