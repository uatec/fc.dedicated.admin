package com.hidef.fc.dedicated.admin.models;

public class ServerConfigPair
{
    public ServerConfigPair(Server server, ServerConfig config) {
        this.server = server;
        this.config = config;
    }

    private Server server;
    private ServerConfig config;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }
}
