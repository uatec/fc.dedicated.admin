package com.hidef.fc.dedicated.admin.task;

import com.hidef.fc.dedicated.admin.subscription.Subscription;
import com.hidef.fc.dedicated.admin.subscription.SubscriptionRepository;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.hateoas.EntityLinks;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RepositoryEventHandler(Task.class)
public class TaskInterceptor {

    private final EntityLinks entityLinks;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    public TaskInterceptor(EntityLinks entityLinks) {
        this.entityLinks = entityLinks;
    }

    @HandleBeforeCreate
    public void beforeNewTask(Task task) {
        System.out.println(task.getId());
    }

    @HandleAfterCreate
    public void newTask(Task task) {
        List<Subscription> subscriptions =  subscriptionRepository.findSubscriptionByEmail(task.getUserEmail());


        if ( subscriptions.size() > 0 ) {

            if (subscriptions.stream().anyMatch(s -> s.getSubscriptionEnds() > new Date().getTime())) {
                System.out.println("Task created with active subscription. Invoking server creation.");

                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.convertAndSend("fc-dedicated-admin",
                        new BeginTaskMessage(task));
            } else {
                System.out.println("Task faulted, no active subscription");
                task.setTaskStatus(TaskStatus.Faulted);
                taskRepository.save(task);
            }
        }
        else {
            System.out.println("Task faulted, no subscription");
            task.setTaskStatus(TaskStatus.Faulted);
            taskRepository.save(task);
        }
    }

    /**
     * Take an {@link Task} and get the URI using Spring Data REST's {@link EntityLinks}.
     *
     * @param task
     */
    private String getPath(Task task) {
        return this.entityLinks.linkForSingleResource(task.getClass(),
                task.getId()).toUri().getPath();
    }
}

