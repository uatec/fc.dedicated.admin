package com.hidef.fc.dedicated.admin.controllers;

import com.hidef.fc.dedicated.admin.SavePaymentMethodRequest;
import com.hidef.fc.dedicated.admin.models.UserProxy;
import com.hidef.fc.dedicated.admin.repositories.UserProxyRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RestController
public class PaymentController
{
    private static UserDetails getPrincipal()
    {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Value("${stripeSecretKey}")
    public String stripeSecretKey;

    @Autowired
    UserProxyRepository userProxyRepository;

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

    @RequestMapping(value = "/api/savepaymentmethod", method = {RequestMethod.POST})
    public void WebHook1(@RequestBody SavePaymentMethodRequest token) throws Exception {

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
