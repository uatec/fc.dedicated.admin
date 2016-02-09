package com.hidef.fc.dedicated.admin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class tokenrequest
{
    public String token;
}


class ServerConfigPair
{
    public ServerConfigPair(Server server, ServerConfig config) {
        this.server = server;
        this.config = config;
    }

    private Server server;
    private ServerConfig config;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }
}


@Component
@RestController
public class StuffController
{
    private static UserDetails getPrincipal()
    {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Value("${stripeSecretKey}")
    public String stripeSecretKey;

    @Value("${awsAccessKey}")
    public String accessKey;

    @Value("${awsSecretKey}")
    public String secretKey;

    @Autowired
    UserProxyRepository userProxyRepository;


    @Autowired
    ServerRepository serverRepository;


    @RequestMapping(value = "/api/deleteserver/{0}", method = {RequestMethod.DELETE})
    public ServerConfigPair DeleteServer(String serverId) throws Exception {
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);

        Optional<ServerConfig> optServerConfig = user.getServerConfig().stream().filter(s -> s.getId().equals(serverId)).findFirst();

        if ( !optServerConfig.isPresent() ) {
            throw new ResourceNotFoundException();
        }

        ServerConfig serverConfig = optServerConfig.get();

        String awsEndpoint = "ec2.eu-west-1.amazonaws.com";
        // get client
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
        amazonEC2Client.setEndpoint(awsEndpoint);

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.withFilters(new Filter("tag:server_id", Collections.singletonList(serverId)));
        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(request);

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(describeInstancesResult.getReservations().get(0).getInstances().get(0).getInstanceId());
        TerminateInstancesResult terminateResult = amazonEC2Client.terminateInstances(terminateInstancesRequest);

        InstanceStateChange isc = terminateResult.getTerminatingInstances().stream().findFirst().get();

        ServerConfig config = serverRepository.findByImplementationId("aws://" + awsEndpoint + "/" + isc.getInstanceId());
        config.setStatus(ServerStatus.Stopping);
        serverRepository.save(config);

        request.withFilters(new Filter("tag:server_id", Collections.singletonList(serverId)));
        DescribeInstancesResult describeInstancesResult2 = amazonEC2Client.describeInstances(request);

        return new ServerConfigPair(instanceToServer(describeInstancesResult2.getReservations().get(0).getInstances().get(0)),
                serverConfig);
    }

    @RequestMapping(value = "/api/createserver", method = {RequestMethod.POST})
    public ServerConfig CreateServer(@RequestBody ServerConfig serverConfig)
    {
        String email = getPrincipal().getUsername();
        serverRepository.save(serverConfig);
        UserProxy user = userProxyRepository.findByEmail(email);
        user.getServerConfig().add(serverConfig);
        userProxyRepository.save(user);

        String newServerUrl = spawnServer(serverConfig, serverConfig.getId(), user.getEmail());
        user.getServerReferences().add(newServerUrl);
        userProxyRepository.save(user);

        serverConfig.setImplementationId(newServerUrl);
        serverRepository.save(serverConfig);
        return serverConfig;
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


    private String spawnServer(ServerConfig serverConfig, String serverId, String clientId) {


        String awsEndpoint = "ec2.eu-west-1.amazonaws.com";
        String imageId = "ami-7943ec0a"; // Microsoft Windows Server 2012 R2 Base


        String instanceSize = "m3.medium";

        String keyName = "fc-dedi-key";
        String securityGroupName = "fc-dedi-group";
        // get client
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
        amazonEC2Client.setEndpoint(awsEndpoint);
        RunInstancesRequest request = new RunInstancesRequest();
        request.withImageId(imageId)
                .withInstanceType(instanceSize)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSecurityGroups(securityGroupName)
                .withUserData("PHBvd2Vyc2hlbGw+DQpjdXJsIGh0dHBzOi8vc3RlYW1jZG4tYS5ha2FtYWloZC5uZXQvY2xpZW50L2luc3RhbGxlci9zdGVhbWNtZC56aXAgLU91dHB1dEZpbGUgc3RlYW1jbWQuemlwDQpBZGQtVHlwZSAtYXNzZW1ibHkgInN5c3RlbS5pby5jb21wcmVzc2lvbi5maWxlc3lzdGVtIg0KW2lvLmNvbXByZXNzaW9uLnppcGZpbGVdOjpFeHRyYWN0VG9EaXJlY3RvcnkoInN0ZWFtY21kLnppcCIsICJjOlxzdGVhbSINCg0KY3VybCBodHRwOi8vcGluYWNsZTgudWF0ZWMubmV0L2ZjZGVkaS50eHQgLU91dHB1dEZpbGUgYzpcc3RlYW1cZmNkZWRpLnR4dA0KDQpjOlxzdGVhbVxzdGVhbWNtZCArcnVuc2NyaXB0IGM6XHN0ZWFtXGZjZGVkaS50eHQNCmNkIGM6XHN0ZWFtXGZjXDY0DQpGQ182NC5leGUgLWJhdGNobW9kZQ0KPC9wb3dlcnNoZWxsPg==");
        RunInstancesResult runInstancesResult =
                amazonEC2Client.runInstances(request);

        CreateTagsRequest createTagsRequest = new CreateTagsRequest(runInstancesResult
                .getReservation()
                .getInstances()
                .stream()
                .map(Instance::getInstanceId)
                .collect(Collectors.toList()),
                Arrays.asList(new Tag("Name", serverConfig.getFriendlyName()),
                        new Tag("client_id", clientId),
                        new Tag("server_id", serverId)));
        amazonEC2Client.createTags(createTagsRequest);

        List<Instance> instances = runInstancesResult.getReservation().getInstances();

        return "vm://aws/" + awsEndpoint + "/" + instances.get(0).getInstanceId();
    }

    @RequestMapping(value = "/api/getservers", method = {RequestMethod.GET})
    public List<ServerConfigPair> GetServerDetails() {
        List<Server> servers = getServers();
        return GetServerConfig().stream().map(c -> {
            Server server = servers
                    .stream()
                    .filter(s -> Objects.equals(s.getId(), c.getId()))
                    .findFirst()
                    .orElse(null);
            return new ServerConfigPair(server, c);
        }).collect(Collectors.toList());
    }

    public List<Server> getServers()
    {
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);



        String awsEndpoint = "ec2.eu-west-1.amazonaws.com";
        // get client
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
        amazonEC2Client.setEndpoint(awsEndpoint);
        // get security group
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.withFilters(new Filter("tag:client_id", Collections.singletonList(user.getEmail())));
        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(request);


        List<Reservation> reservations = describeInstancesResult.getReservations();


        if ( reservations.size() == 0 )
        {
            return new ArrayList<>();
        }

        return  reservations
                .stream()
                .map(Reservation::getInstances)
                .flatMap(List::stream)
                .map(i -> instanceToServer(i))
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

    @RequestMapping(value = "/api/getserverconfigs", method = {RequestMethod.GET})
    public List<ServerConfig> GetServerConfig() {
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);
        if ( user.getServerConfig().size() > 0 ) {
            return user.getServerConfig().stream().collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }


    @RequestMapping(value = "/api/paymentmethods", method = {RequestMethod.GET})
    public List<ExternalAccount> GetPaymentMethods() throws URISyntaxException, CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {

        // TODO mask this strip response object
        Stripe.apiKey = this.stripeSecretKey;
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);
        if ( user.getPaymentReferences().size() > 0 ) {
            URI firstPayment = new URI(user.getPaymentReferences().stream().findFirst().get());
            Customer customer = Customer.retrieve(firstPayment.getAuthority());
            return customer.getSources().getData();
        } else {
            return new ArrayList<>();
        }
    }

    // get or create user object
    @RequestMapping(value = "/api/user", method = {RequestMethod.GET})
    public UserProxy GetOrCreateUser()
    {
        System.out.println("get or create user");
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);
        if ( user == null )
        {
            user = new UserProxy();
            user.setEmail(email);
            userProxyRepository.save(user);
        }

        return user;
    }

    @RequestMapping(value = "/api/savepaymentmethod", method = {RequestMethod.POST})
    public void WebHook1(@RequestBody tokenrequest token) throws Exception {

        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);
        Stripe.apiKey = this.stripeSecretKey;

        // save payment details in stripe
        Customer customer = getOrCreateCustomer(user);
        Map<String, Object> params = new HashMap<>();
        params.put("source", token.token);
        Card paymentMethod = customer.createCard(params);

        // Save a reference in our database
        user.getPaymentReferences().add(new URI("stripe://" + customer.getId() + "/" + paymentMethod.getId()).toString());
        userProxyRepository.save(user);
    }

    private Customer getOrCreateCustomer(UserProxy user) throws URISyntaxException, CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {

        if ( user.getPaymentReferences().size() > 0 ) {
            URI firstPayment = new URI(user.getPaymentReferences().stream().findFirst().get());
            return Customer.retrieve(firstPayment.getHost());
        } else {
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("email", user.getEmail());
            return Customer.create(customerParams);
        }
    }
}
