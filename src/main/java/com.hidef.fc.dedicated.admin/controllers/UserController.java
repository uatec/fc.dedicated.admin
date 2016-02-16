package com.hidef.fc.dedicated.admin.controllers;

import com.hidef.fc.dedicated.admin.repositories.ServerRepository;
import com.hidef.fc.dedicated.admin.models.UserProxy;
import com.hidef.fc.dedicated.admin.repositories.UserProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@Component
@RestController
public class UserController
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
            user.setId(UUID.randomUUID().toString());
            user.setEmail(email);
            userProxyRepository.save(user);
        }

        return user;
    }
}
