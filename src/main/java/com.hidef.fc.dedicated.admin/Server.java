package com.hidef.fc.dedicated.admin;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Server {

    private @Id
    @GeneratedValue
    String id;
    private String friendlyName;
    private ServerStatus status = ServerStatus.Shiny;
    private String ownerEmail;

    public Server() {}

    public Server(String id, String friendlyName, ServerStatus status) {
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}