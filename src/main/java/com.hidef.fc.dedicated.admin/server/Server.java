package com.hidef.fc.dedicated.admin.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import org.joda.time.DateTime;

@Data
@Entity
public class Server {

    private @Id @GeneratedValue String id;
    private String friendlyName;
    private ServerStatus actualStatus = ServerStatus.Shiny;
    private ServerStatus desiredStatus = ServerStatus.Shiny;
    private String ownerEmail;

    public Server() {}

    public Server(String id, String friendlyName, ServerStatus actualStatus) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.actualStatus = actualStatus;
    }

    public ServerStatus getActualStatus() {
        return actualStatus;
    }

    public void setActualStatus(ServerStatus actualStatus) {
        this.actualStatus = actualStatus;
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

    public ServerStatus getDesiredStatus() {
        return desiredStatus;
    }

    public void setDesiredStatus(ServerStatus desiredStatus) {
        this.desiredStatus = desiredStatus;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}

