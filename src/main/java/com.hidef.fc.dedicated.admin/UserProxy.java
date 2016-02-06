package com.hidef.fc.dedicated.admin;

import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
public class UserProxy
{
    public @Id
    @GeneratedValue
    String id;

    String email;

    public Set<ServerConfig> getServerConfig() {
        return serverConfig;
    }

    public void getServerConfig(Set<ServerConfig> serverConfigConfig) {
        this.serverConfig = serverConfigConfig;
    }

    public List<String> getPaymentReferences() {
        return paymentReferences;
    }

    public void setPaymentReferences(List<String> paymentReferences) {
        this.paymentReferences = paymentReferences;
    }

    public List<String> getIdentityReferences() {
        return identityReferences;
    }

    public void setIdentityReferences(List<String> identityReferences) {
        this.identityReferences = identityReferences;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ElementCollection
    List<String> identityReferences = new ArrayList<>();

    @ElementCollection
    List<String> paymentReferences = new ArrayList<>();

    @OneToMany
    Set<ServerConfig> serverConfig = new HashSet<>();

    public void setServerConfig(Set<ServerConfig> serverConfig) {
        this.serverConfig = serverConfig;
    }

    public List<String> getServerReferences() {
        return serverReferences;
    }

    public void setServerReferences(List<String> serverReferences) {
        this.serverReferences = serverReferences;
    }

    @ElementCollection
    List<String> serverReferences = new ArrayList<>();


}

