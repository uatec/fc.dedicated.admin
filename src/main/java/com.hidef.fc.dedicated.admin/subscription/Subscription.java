package com.hidef.fc.dedicated.admin.subscription;

import lombok.Data;
import org.joda.time.DateTime;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Data
@Entity
public class Subscription
{
    private @Id @GeneratedValue String id;
    private String token;
    private String plan;
    private String email;
    private DateTime subscriptionEnds;

    public Subscription() {}

    public Subscription(String id, String token, String plan, String email, DateTime subscriptionEnds) {
        this.id = id;
        this.token = token;
        this.plan = plan;
        this.email = email;
        this.subscriptionEnds = subscriptionEnds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DateTime getSubscriptionEnds() {
        return subscriptionEnds;
    }

    public void setSubscriptionEnds(DateTime subscriptionEnds) {
        this.subscriptionEnds = subscriptionEnds;
    }
}
