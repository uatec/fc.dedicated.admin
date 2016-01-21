package com.hidef.fc.dedicated.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hidef.fc.dedicated.admin.server.ServerRepository;
import com.hidef.fc.dedicated.admin.subscription.Subscription;
import com.hidef.fc.dedicated.admin.subscription.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.net.APIResource;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@Component
@RestController
public class WebHookController
{
    @Autowired
    public SubscriptionRepository subscriptionRepository;

    @Value("${stripeSecretKey}")
    public String stripeSecretKey;



    @RequestMapping(value = "/webhooks", method = {RequestMethod.POST})
    public void WebHook1(@RequestBody String data) throws Exception {
        Stripe.apiKey = this.stripeSecretKey;
        Event eventJson = APIResource.GSON.fromJson(data, Event.class);

        Event event = Event.retrieve(eventJson.getId());
        if ( event == null ) throw new Exception("Some bad shit happened, couldn't find your payment.");

        switch (eventJson.getType())
        {
            case "customer.subscription.created":
                com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) event.getData().getObject();

                Customer customer = Customer.retrieve(subscription.getCustomer());

                subscription.getCurrentPeriodEnd();
                subscription.getPlan();
                customer.getEmail();

                System.out.println("Retreiving subscriptions for " + customer.getEmail());

                List<Subscription> subscriptions =  subscriptionRepository.findSubscriptionByEmail(customer.getEmail());

                Subscription mySubscription = subscriptions.stream().findFirst().get();
                System.out.println("Found subscription: " + mySubscription.getId());

                mySubscription.setSubscriptionEnds(subscription.getCurrentPeriodEnd());

                System.out.println("updated subscription end time to: " + mySubscription.getSubscriptionEnds());

                subscriptionRepository.save(mySubscription);

                System.out.println("Subscription saved");

                break;

            default:
                break;
        }
    }
}
