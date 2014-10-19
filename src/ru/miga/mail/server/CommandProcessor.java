package ru.miga.mail.server;

public interface CommandProcessor {

    public void initConnection(ServerConnectionThread serverConnectionThread);

    public void hello();

    public void quit();

    public void process(String command);
}
