package com.hidef.fc.dedicated.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.hidef.fc.dedicated.admin,com.auth0")
@ImportResource("classpath:auth0-security-context.xml")
@PropertySource("classpath:auth0.properties")
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
    }
}

