package com.hidef.fc.dedicated.admin.services;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.hidef.fc.dedicated.admin.models.Server;
import com.hidef.fc.dedicated.admin.models.ServerConfig;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AWSVMService implements IVMService {

    @Value("${awsAccessKey}")
    public String accessKey;

    @Value("${awsSecretKey}")
    public String secretKey;

    @Value("${awsEndpoint}")
    public String awsEndpoint;

    private AmazonEC2Client _amazonEC2Client;

    private AmazonEC2Client getAmazonEC2Client()
    {
        if ( _amazonEC2Client == null )
        {
            _amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
            _amazonEC2Client.setEndpoint(this.awsEndpoint);
        }

        return _amazonEC2Client;
    }

    @Override
    public Server Delete(String serverId) {
        // prepare describe request
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.withFilters(new Filter("tag:server_id", Collections.singletonList(serverId)));

        // get the full details
        DescribeInstancesResult describeInstancesResult = getAmazonEC2Client().describeInstances(request);

        // terminate that instance
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(describeInstancesResult.getReservations().get(0).getInstances().get(0).getInstanceId());
        TerminateInstancesResult terminateResult = getAmazonEC2Client().terminateInstances(terminateInstancesRequest);
        InstanceStateChange isc = terminateResult.getTerminatingInstances().stream().findFirst().get();

        // get the full details again. just to check. (really necessary?)
        DescribeInstancesResult describeInstancesResult2 = getAmazonEC2Client().describeInstances(request);

        // map to domain object before returning
        return instanceToServer(describeInstancesResult2.getReservations().get(0).getInstances().get(0));
    }

    @Override
    public String spawnServer(ServerConfig serverConfig, String serverId, String clientId) {

        String imageId = "ami-7943ec0a"; // Microsoft Windows Server 2012 R2 Base


        String instanceSize = "m3.medium";

        String keyName = "fc-dedi-key";
        String securityGroupName = "fc-dedi-group";

        // create the instance itself
        RunInstancesRequest request = new RunInstancesRequest();
        request.withImageId(imageId)
                .withInstanceType(instanceSize)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSecurityGroups(securityGroupName)
                .withUserData("PHBvd2Vyc2hlbGw+DQpjdXJsIGh0dHBzOi8vc3RlYW1jZG4tYS5ha2FtYWloZC5uZXQvY2xpZW50L2luc3RhbGxlci9zdGVhbWNtZC56aXAgLU91dHB1dEZpbGUgc3RlYW1jbWQuemlwDQpBZGQtVHlwZSAtYXNzZW1ibHkgInN5c3RlbS5pby5jb21wcmVzc2lvbi5maWxlc3lzdGVtIg0KW2lvLmNvbXByZXNzaW9uLnppcGZpbGVdOjpFeHRyYWN0VG9EaXJlY3RvcnkoInN0ZWFtY21kLnppcCIsICJjOlxzdGVhbSINCg0KY3VybCBodHRwOi8vcGluYWNsZTgudWF0ZWMubmV0L2ZjZGVkaS50eHQgLU91dHB1dEZpbGUgYzpcc3RlYW1cZmNkZWRpLnR4dA0KDQpjOlxzdGVhbVxzdGVhbWNtZCArcnVuc2NyaXB0IGM6XHN0ZWFtXGZjZGVkaS50eHQNCmNkIGM6XHN0ZWFtXGZjXDY0DQpGQ182NC5leGUgLWJhdGNobW9kZQ0KPC9wb3dlcnNoZWxsPg==");
        RunInstancesResult runInstancesResult =
                getAmazonEC2Client().runInstances(request);



        // tag this instance so we can find it again later
        CreateTagsRequest createTagsRequest = new CreateTagsRequest(runInstancesResult
                .getReservation()
                .getInstances()
                .stream()
                .map(Instance::getInstanceId)
                .collect(Collectors.toList()),
                Arrays.asList(new Tag("Name", serverConfig.getFriendlyName()),
                        new Tag("client_id", clientId),
                        new Tag("server_id", serverId)));

        getAmazonEC2Client().createTags(createTagsRequest);

        List<Instance> instances = runInstancesResult.getReservation().getInstances();

        return "vm://aws/" + awsEndpoint + "/" + instances.get(0).getInstanceId();
    }

    @Override
    public List<Server> getServers(String userId) {

        // get security group
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.withFilters(new Filter("tag:client_id", Collections.singletonList(userId)));
        DescribeInstancesResult describeInstancesResult = getAmazonEC2Client().describeInstances(request);

        List<Reservation> reservations = describeInstancesResult.getReservations();

        if (reservations.size() == 0) {
            return new ArrayList<>();
        }

        return reservations
                .stream()
                .map(Reservation::getInstances)
                .flatMap(List::stream)
                .map(this::instanceToServer)
                .collect(Collectors.toList());
    }

    private Server instanceToServer(Instance i) {
        Server server = new Server();
        server.setDnsName(i.getPublicDnsName());
        Map<String, String> tagMap = toMap(i.getTags(), Tag::getKey, Tag::getValue);
        server.setId(tagMap.get("server_id"));
        server.setInstanceType(i.getInstanceType());
        server.setStatus(i.getState().getName());
        return server;
    }

    private <O, K, V> Map<K, V> toMap(List<O> original, Function<? super O, ? extends K> keyFunc, Function<? super O, ? extends V> valFunc)
    {
        Map<K,V> map = new HashMap<>();
        for (O i : original) map.put(keyFunc.apply(i), valFunc.apply(i));
        return map;
    }

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
}
