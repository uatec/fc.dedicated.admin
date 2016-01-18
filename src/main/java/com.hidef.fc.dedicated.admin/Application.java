package com.hidef.fc.dedicated.admin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.hidef.fc.dedicated.admin.task.BeginTaskMessage;
import com.stripe.Stripe;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootApplication
public class Application {

    private final String queueName = "fc-dedicated-admin";

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(queueName);
    }

    @Bean
    MessageConverter messageconverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver,
                                           MessageConverter messageConverter) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver, "receiveMessage");
        listenerAdapter.setMessageConverter(messageConverter);
        return listenerAdapter;
    }


    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
    }
}

@Component
class Receiver {


    private void openPort(AmazonEC2Client client, String securityGroup, String protocol, int port)
    {
        IpPermission ipPermission =
                new IpPermission();

        ipPermission.withIpRanges("0.0.0.0/0")
                .withIpProtocol(protocol)
                .withFromPort(port)
                .withToPort(port);


        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
                new AuthorizeSecurityGroupIngressRequest();

        authorizeSecurityGroupIngressRequest.withGroupName(securityGroup)
                .withIpPermissions(ipPermission);

        client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

    }


    @Value("${awsAccessKey}")
    public String accessKey;
    @Value("${awsSecretKey}")

    public String secretKey;

    @RabbitListener(queues = "fc-dedicated-admin")
    public void receiveMessage(@Payload BeginTaskMessage beginTaskMessage) throws IOException {
        System.out.println("Message handled: " + beginTaskMessage.getTask().getServerId() + " - " + beginTaskMessage.getTask().getAction());
//
//
//        String awsEndpoint = "ec2.us-west-2.amazonaws.com";
//        String imageId = "ami-83a5bce2";


        String awsEndpoint = "ec2.eu-west-1.amazonaws.com";
        String imageId = "ami-7943ec0a"; // Microsoft Windows Server 2012 R2 Base


        String instanceSize = "m3.medium";

        String keyName = "fc-dedi-key";
        String securityGroupName = "fc-dedi-group";

        // get client
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
        amazonEC2Client.setEndpoint(awsEndpoint);
        // get security group


//        CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
//
//        csgr.withGroupName(securityGroupName).withDescription(securityGroupName);
//        CreateSecurityGroupResult createSecurityGroupResult =
//                amazonEC2Client.createSecurityGroup(csgr);
//
//
//        openPort(amazonEC2Client, securityGroupName, "tcp", 3389);
//        openPort(amazonEC2Client, securityGroupName, "udp", 27012);
//
//                // get key pair
//
//        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
//
//        createKeyPairRequest.withKeyName(keyName);
//
//        CreateKeyPairResult createKeyPairResult =
//                amazonEC2Client.createKeyPair(createKeyPairRequest);
//
//        KeyPair keyPair = new KeyPair();
//
//        keyPair = createKeyPairResult.getKeyPair();
//
//        String privateKey = keyPair.getKeyMaterial();
//
//        System.out.println("New Private Key Generated for: " + keyPair.getKeyName());
//
//        System.out.println(privateKey);

        // start instance
        // -- set environment variables in instance?
        // -- deploy and install FC
        // -- Launch FC with configuration from environment variables




        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId(imageId)
                .withInstanceType(instanceSize)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSecurityGroups(securityGroupName)
                .withUserData("PHBvd2Vyc2hlbGw+DQpjdXJsIGh0dHBzOi8vc3RlYW1jZG4tYS5ha2FtYWloZC5uZXQvY2xpZW50L2luc3RhbGxlci9zdGVhbWNtZC56aXAgLU91dHB1dEZpbGUgc3RlYW1jbWQuemlwDQpBZGQtVHlwZSAtYXNzZW1ibHkgInN5c3RlbS5pby5jb21wcmVzc2lvbi5maWxlc3lzdGVtIg0KW2lvLmNvbXByZXNzaW9uLnppcGZpbGVdOjpFeHRyYWN0VG9EaXJlY3RvcnkoInN0ZWFtY21kLnppcCIsICJjOlxzdGVhbSINCg0KY3VybCBodHRwOi8vcGluYWNsZTgudWF0ZWMubmV0L2ZjZGVkaS50eHQgLU91dHB1dEZpbGUgYzpcc3RlYW1cZmNkZWRpLnR4dA0KDQpjOlxzdGVhbVxzdGVhbWNtZCArcnVuc2NyaXB0IGM6XHN0ZWFtXGZjZGVkaS50eHQNCmNkIGM6XHN0ZWFtXGZjXDY0DQpGQ182NC5leGUgLWJhdGNobW9kZQ0KPC9wb3dlcnNoZWxsPg==");

        RunInstancesResult runInstancesResult =
                amazonEC2Client.runInstances(runInstancesRequest);

        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        System.out.println(instances.get(0).getInstanceId());

        System.out.println(runInstancesRequest.toString());

        CreateTagsRequest createTagsRequest = new CreateTagsRequest(runInstancesResult
        .getReservation()
        .getInstances()
                .stream()
                .map(Instance::getInstanceId)
                .collect(Collectors.toList()),
                Arrays.asList(new Tag("Name", beginTaskMessage.getTask().getServerId())));


        amazonEC2Client.createTags(createTagsRequest);

    }
}

