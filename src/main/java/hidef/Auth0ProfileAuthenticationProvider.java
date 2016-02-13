package hidef;

import com.auth0.jwt.JWTVerifier;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created by uatec on 10/02/16.
 */
public class Auth0ProfileAuthenticationProvider implements AuthenticationProvider, InitializingBean {
    private String clientSecret = null;
    private String clientId = null;
    private String securedRoute = null;
    private final Log logger = LogFactory.getLog(this.getClass());

    @Value("${auth0.domain}")
    String auth0Domain;

    public Auth0ProfileAuthenticationProvider() {
    }

    private JSONObject getProfile(Auth0JWTToken token) throws UnirestException {
        String response = Unirest
                .post("https://" + auth0Domain + "/tokeninfo")
                .header("Content-Type", "application/json")
                .body(new tokenrequest(token.getJwt()))
                .asString()
                .getBody();

        return Unirest
                .post("https://" + auth0Domain + "/tokeninfo")
                .header("Content-Type", "application/json")
                .body(new tokenrequest(token.getJwt()))
                .asJson()
                .getBody()
                .getObject();
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = ((Auth0JWTToken)authentication).getJwt();
        this.logger.info("Trying to authenticate with token: " + token);

        try {
            Auth0JWTToken e = (Auth0JWTToken)authentication;
            JSONObject decoded = this.getProfile(e);
            this.logger.debug("Decoded JWT token" + decoded);
            e.setAuthenticated(true);
            e.setPrincipal(new Auth0UserDetails(decoded));
            e.setDetails(decoded);
            return authentication;
        } catch (UnirestException e) {
            this.logger.debug("IOException thrown while decoding JWT token " + e.getLocalizedMessage());
            throw new Auth0TokenException("Authentication error occured");
        }
    }

    public boolean supports(Class<?> authentication) {
        return Auth0JWTToken.class.isAssignableFrom(authentication);
    }

    public String getSecuredRoute() {
        return this.securedRoute;
    }

    public void setSecuredRoute(String securedRoute) {
        this.securedRoute = securedRoute;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
