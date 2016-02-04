package com.hidef.fc.dedicated.admin;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCardCollection;
import com.stripe.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

class tokenrequest
{
    public String token;
}

@Component
@RestController
public class StuffController
{
    @Value("${stripeSecretKey}")
    public String stripeSecretKey;


    @RequestMapping(value = "/api/paymentmethods", method = {RequestMethod.GET})
    public List<ExternalAccount> GetPaymentMethods() throws URISyntaxException, CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {

        // TODO mask this strip response object
        Stripe.apiKey = this.stripeSecretKey;
        String email = "uatecuk@gmail.com";
        UserProxy user = userProxyRepository.findByEmail(email);
        if ( user.getPaymentReferences().size() > 0 ) {
            URI firstPayment = new URI(user.getPaymentReferences().stream().findFirst().get());
            Customer customer = Customer.retrieve(firstPayment.getAuthority());
            return customer.getSources().getData();
        } else {
            return new ArrayList<>();
        }
    }

    @Autowired
    UserProxyRepository userProxyRepository;

    // get or create user object
    @RequestMapping(value = "/api/user", method = {RequestMethod.GET})
    public UserProxy GetOrCreateUser()
    {
        System.out.println("get or create user");
        String email = "uatecuk@gmail.com";
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
        String email = "uatecuk@gmail.com";
        UserProxy user = userProxyRepository.findByEmail(email);
        Stripe.apiKey = this.stripeSecretKey;

        // save payment details in stripe
        Customer customer = getOrCreateCustomer(user);
        Map<String, Object> params = new HashMap<String, Object>();
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
