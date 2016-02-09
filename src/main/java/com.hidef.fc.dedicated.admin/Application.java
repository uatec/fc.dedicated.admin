package com.hidef.fc.dedicated.admin;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import javax.servlet.Filter;

@Configuration
@ComponentScan("com.hidef.fc.dedicated.admin,com.auth0")
@ImportResource("classpath:auth0-security-context.xml")
@SpringBootApplication
public class Application {

    @Bean
    public SimpleCORSFilter simpleCORSFilter()
    {
        return new SimpleCORSFilter();
    }

    @Bean
    public FilterRegistrationBean securityFilterChain(SimpleCORSFilter securityFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(securityFilter);
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
    }
}

