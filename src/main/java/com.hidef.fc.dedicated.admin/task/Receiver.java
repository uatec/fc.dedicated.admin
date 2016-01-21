package com.hidef.fc.dedicated.admin.task;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.hidef.fc.dedicated.admin.server.Server;
import com.hidef.fc.dedicated.admin.server.ServerRepository;
import com.hidef.fc.dedicated.admin.server.ServerStatus;
import com.hidef.fc.dedicated.admin.task.BeginTaskMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Receiver {


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

    @Autowired
    public TaskRepository taskRepository;

    @Autowired
    public ServerRepository serverRepository;

    @RabbitListener(queues = "fc-dedicated-admin")
    public void receiveMessage(@Payload BeginTaskMessage beginTaskMessage) throws IOException {
        System.out.println("Message handled: " + beginTaskMessage.getTask().getServerId() + " - " + beginTaskMessage.getTask().getAction());

        Task t = beginTaskMessage.getTask();
        t.setTaskStatus(TaskStatus.Running);
        taskRepository.save(t);

        Server server = new Server();
        server.setFriendlyName(t.getServerId());
        server.setOwnerEmail(t.getUserEmail());
        server.setDesiredStatus(ServerStatus.Running);
        server.setActualStatus(ServerStatus.Building);
        serverRepository.save(server);
        try {

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


            server.setActualStatus(ServerStatus.Starting);
            serverRepository.save(server);
            t.setTaskStatus(TaskStatus.Succeeded);
            taskRepository.save(t);
        }
        catch ( Exception ex)
        {
            t.setTaskStatus(TaskStatus.Faulted);
            t.setMessage(ex.getMessage());
            taskRepository.save(t);
        }

    }
}
