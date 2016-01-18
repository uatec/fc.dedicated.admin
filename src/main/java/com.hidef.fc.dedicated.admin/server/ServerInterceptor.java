package com.hidef.fc.dedicated.admin.server;

import com.hidef.fc.dedicated.admin.subscription.Subscription;
import com.hidef.fc.dedicated.admin.subscription.SubscriptionRepository;
import com.hidef.fc.dedicated.admin.task.BeginTaskMessage;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@RepositoryEventHandler(Server.class)
public class ServerInterceptor {

    private final EntityLinks entityLinks;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    public SubscriptionRepository subscriptionRepository;

    @Autowired
    public ServerInterceptor(EntityLinks entityLinks) {
        this.entityLinks = entityLinks;
    }

    @HandleAfterSave
    public void saveSubscription(Server server) {

        Function<Server, Boolean> behaviour = statusCase(server);

        if ( behaviour != null ) behaviour.apply(server);

    }

    @HandleAfterCreate
    public void newSubscription(Server server) {
    }

    /**
     * Take an {@link Server} and get the URI using Spring Data REST's {@link EntityLinks}.h
     *
     * @param server
     */
    private String getPath(Server server) {
        return this.entityLinks.linkForSingleResource(server.getClass(),
                server.getId()).toUri().getPath();
    }

    private Function<Server, Boolean> statusCase(Server server){
        switch (server.getDesiredStatus())
        {
            case Running:
                switch ( server.getActualStatus() )
                {
                    case Shiny:
                        return Server -> {
                            // create server
                            //
//                            Optional<Subscription> subscriptionOptional = subscriptionRepository.findSubscriptionByEmail(server.getOwnerEmail());
//
//                            if ( subscriptionOptional.isPresent() ) {
//                                if (subscriptionOptional.get().getSubscriptionEnds().isAfter(DateTime.now())) {
//                                    System.out.println("Task created with active subscription. Invoking server creation.");
//
//                                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//                                    rabbitTemplate.convertAndSend("fc-dedicated-admin",
//                                            new BeginTaskMessage(new Task));
//                                } else {
//                                    System.out.println("Task created with inactive subscription.");
//                                }
//                            }
//                            else {
//                                System.out.println("Task created without subscription.");
//                            }
                            return false;
                        };
                    case Stopped:
                    return (Server) -> {
                      return false;
                    };
                }
                break;
        }
        return null;
    }
}

