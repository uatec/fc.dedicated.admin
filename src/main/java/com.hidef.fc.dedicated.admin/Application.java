package com.hidef.fc.dedicated.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hidef.fc.dedicated.admin.controllers.AWSVMService;
import com.hidef.fc.dedicated.admin.controllers.IVMService;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import hidef.Auth0AuthenticationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationManager;

import java.io.IOException;

@Configuration
@ComponentScan("com.hidef.fc.dedicated.admin,com.hidef")
@ImportResource("classpath:application-context.xml")
@SpringBootApplication
public class Application {

    @Bean
    public IVMService vmService()
    {
        return new AWSVMService();
    }

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

    @Bean
    public Auth0AuthenticationFilter auth0AuthenticationFilter()
    {
        return new Auth0AuthenticationFilter();
    }

    @Bean
    public FilterRegistrationBean securityFilterChain2(Auth0AuthenticationFilter auth0AuthenticationFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(auth0AuthenticationFilter);
        registration.setOrder(Integer.MIN_VALUE + 1);
        return registration;
    }


    public static void main(String[] args) throws InterruptedException {

        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        SpringApplication.run(Application.class, args);
    }
}

