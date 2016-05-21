package com.hidef.fc.dedicated.admin.models;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Data
public class UserProxy
{
    public @Id
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

    List<String> identityReferences = new ArrayList<>();

    List<String> paymentReferences = new ArrayList<>();

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

    List<String> serverReferences = new ArrayList<>();


}

