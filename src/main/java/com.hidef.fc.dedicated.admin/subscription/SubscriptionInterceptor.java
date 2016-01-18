package com.hidef.fc.dedicated.admin.subscription;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RepositoryEventHandler(Subscription.class)
public class SubscriptionInterceptor {

    private final EntityLinks entityLinks;

    @Autowired
    public SubscriptionInterceptor(EntityLinks entityLinks) {
        this.entityLinks = entityLinks;
    }

    @HandleAfterSave
    public void saveSubscription(Subscription subscription) throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
        System.out.println("Save Subscription: " + subscription.getId());


//        customer.
    }

    @HandleAfterCreate
    public void newSubscription(Subscription subscription) throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
        System.out.println("New Subscription: " + subscription.getId());

        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("source", subscription.getToken());
        customerParams.put("plan", subscription.getPlan());
        customerParams.put("email", subscription.getEmail());

        Customer customer = Customer.create(customerParams);
        System.out.println("Customer Created: " + customer.getId());
    }

    /**
     * Take an {@link Subscription} and get the URI using Spring Data REST's {@link EntityLinks}.h
     *
     * @param subscription
     */
    private String getPath(Subscription subscription) {
        return this.entityLinks.linkForSingleResource(subscription.getClass(),
                subscription.getId()).toUri().getPath();
    }
}

