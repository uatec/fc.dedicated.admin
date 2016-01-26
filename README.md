### Configuration ###

application.properties
```
awsAccessKey
awsSecretKey

spring.rabbitmq.addresses
stripeSecretKey
```
auth0.properties
```
auth0.clientSecret
auth0.clientId
auth0.domain
auth0.securedRoute=/secured/**
```

### Running the Service ###

This is a SpringBoot project and can be started simply:


```
mvn springboot:run
```