package com.hidef.fc.dedicated.admin.controllers;

import com.hidef.fc.dedicated.admin.NoPaymentException;
import com.hidef.fc.dedicated.admin.ResourceNotFoundException;
import com.hidef.fc.dedicated.admin.models.*;
import com.hidef.fc.dedicated.admin.repositories.ServerRepository;
import com.hidef.fc.dedicated.admin.repositories.UserProxyRepository;
import com.hidef.fc.dedicated.admin.services.IVMService;
import com.stripe.Stripe;
import com.stripe.exception.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RestController
public class ServerController
{
    private static UserDetails getPrincipal()
    {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Autowired
    UserProxyRepository userProxyRepository;


    @Autowired
    ServerRepository serverRepository;

    @Value("${stripeSecretKey}")
    public String stripeSecretKey;

    @Autowired
    IVMService vmService;

    @RequestMapping(value = "/api/deleteserver/{0}", method = {RequestMethod.DELETE})
    public ServerConfigPair DeleteServer(String serverId) throws Exception {

        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);

        Optional<ServerConfig> optServerConfig = user.getServerConfig().stream().filter(s -> s.getId().equals(serverId)).findFirst();

        if ( !optServerConfig.isPresent() ) {
            throw new ResourceNotFoundException();
        }

        ServerConfig serverConfig = optServerConfig.get();


        Server server = vmService.Delete(serverId);

        ServerConfig config = serverRepository.findByImplementationId(server.getId());
        config.setStatus(ServerStatus.Stopped);
        serverRepository.save(config);

        return new ServerConfigPair(server, serverConfig);
    }

    @RequestMapping(value = "/api/createserver", method = {RequestMethod.POST})
    public ServerConfig CreateServer(@RequestBody ServerConfig serverConfig) {
        String email = getPrincipal().getUsername();
        UserProxy user = userProxyRepository.findByEmail(email);

        // check that the user has a payment method
        if ( user.getPaymentReferences().size() == 0)
        {
            throw new NoPaymentException();
        }

        serverRepository.save(serverConfig);
        user.getServerConfig().add(serverConfig);
        userProxyRepository.save(user);


        String newServerUrl = vmService.spawnServer(serverConfig, serverConfig.getId(), user.getEmail());
        user.getServerReferences().add(newServerUrl);
        userProxyRepository.save(user);

        serverConfig.setImplementationId(newServerUrl);
        serverRepository.save(serverConfig);
        return serverConfig;
    }

    @RequestMapping(value = "/api/getservers", method = {RequestMethod.GET})
    public List<ServerConfigPair> GetServerDetails() {
        String username = getPrincipal().getUsername();
        // check user still exists in database
        UserProxy user = userProxyRepository.findByEmail(username);

        List<Server> servers = vmService.getServers(user.getEmail());
        return GetServerConfig().stream().map(c -> {
            Server server = servers
                    .stream()
                    .filter(s -> Objects.equals(s.getId(), c.getId()))
                    .findFirst()
                    .orElse(null);
            return new ServerConfigPair(server, c);
        }).collect(Collectors.toList());
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
}
