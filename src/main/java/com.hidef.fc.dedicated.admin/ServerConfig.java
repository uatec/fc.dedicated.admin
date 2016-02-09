package com.hidef.fc.dedicated.admin;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class ServerConfig {

    private @Id
    String id;
    private String friendlyName;
    private ServerStatus status = ServerStatus.Shiny;

    public ServerConfig() {}

    public ServerConfig(String id, String friendlyName, ServerStatus status) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.status = status;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }
}
