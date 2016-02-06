package com.hidef.fc.dedicated.admin;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

class tokenrequest
{
    public String token;
}


class ServerConfigPair
{
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


    @RequestMapping(value = "/api/createserver", method = {RequestMethod.POST})
    public ServerConfig CreateServer(@RequestBody ServerConfig serverConfig)
    {
        String email = getPrincipal().getUsername();
        serverRepository.save(serverConfig);
        UserProxy user = userProxyRepository.findByEmail(email);
        user.getServerConfig().add(serverConfig);
        userProxyRepository.save(user);
        return serverConfig;
    }

    @RequestMapping(value = "/api/getservers", method = {RequestMethod.GET})
    public List<ServerConfigPair> GetServerDetails() {
        List<Server> servers = getServers();
        return GetServerConfig().stream().map(c -> {
            ServerConfigPair pair = new ServerConfigPair();
            pair.setConfig(c);
            Server server = servers
                    .stream()
                    .filter(s -> Objects.equals(s.getId(), c.getId()))
                    .findFirst()
                    .orElse(null);
            pair.setServer(server);
            return pair;
        }).collect(Collectors.toList());
    }

    public List<Server> getServers()
    {
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);



        String awsEndpoint = "ec2.eu-west-1.amazonaws.com";
        String imageId = "ami-7943ec0a"; // Microsoft Windows Server 2012 R2 Base


        String instanceSize = "m3.medium";

        String keyName = "fc-dedi-key";
        String securityGroupName = "fc-dedi-group";
        // get client
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
        amazonEC2Client.setEndpoint(awsEndpoint);
        // get security group

        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances();

        return describeInstancesResult.getReservations()
                .stream()
                .findFirst()
                .get()
                .getInstances()
                .parallelStream()
                .map((Instance i) -> {
                    Server server = new Server();
                    server.setDnsName(i.getPublicDnsName());
                    server.setId(i.getInstanceId());
                    server.setInstanceType(i.getInstanceType());
                    server.setStatus(i.getState().getName());
                    return server;
                })
                .collect(Collectors.toList());
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
