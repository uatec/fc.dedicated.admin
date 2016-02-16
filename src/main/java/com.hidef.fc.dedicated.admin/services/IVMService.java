package com.hidef.fc.dedicated.admin.services;

import com.hidef.fc.dedicated.admin.models.Server;
import com.hidef.fc.dedicated.admin.models.ServerConfig;

import java.util.List;

public interface IVMService {
    Server Delete(String id);
    String spawnServer(ServerConfig serverConfig, String serverId, String clientId);
    List<Server> getServers(String userId);
}
