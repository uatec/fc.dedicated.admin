package com.hidef.fc.dedicated.admin;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Data
@Entity
public class UserProxy
{
    public @Id
    @GeneratedValue
    String id;

    String email;

    public Set<Server> getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(Set<Server> serverConfig) {
        this.serverConfig = serverConfig;
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
    Set<Server> serverConfig = new HashSet<>();



}

