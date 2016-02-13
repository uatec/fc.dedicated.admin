package hidef;

import com.auth0.jwt.JWTVerifier;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public class Auth0AuthenticationProvider implements AuthenticationProvider, InitializingBean {
    private JWTVerifier jwtVerifier = null;
    private String clientSecret = null;
    private String clientId = null;
    private String securedRoute = null;
    private final Log logger = LogFactory.getLog(this.getClass());
    private static final AuthenticationException AUTH_ERROR = new Auth0TokenException("Authentication error occured");

    public Auth0AuthenticationProvider() {
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = ((Auth0JWTToken)authentication).getJwt();
        this.logger.info("Trying to authenticate with token: " + token);

        try {
            Auth0JWTToken e = (Auth0JWTToken)authentication;
            Map decoded = this.jwtVerifier.verify(token);
            this.logger.debug("Decoded JWT token" + decoded);
            e.setAuthenticated(true);
            e.setPrincipal(new Auth0UserDetails(decoded));
            e.setDetails(decoded);
            return authentication;
        } catch (InvalidKeyException var5) {
            this.logger.debug("InvalidKeyException thrown while decoding JWT token " + var5.getLocalizedMessage());
            throw AUTH_ERROR;
        } catch (NoSuchAlgorithmException var6) {
            this.logger.debug("NoSuchAlgorithmException thrown while decoding JWT token " + var6.getLocalizedMessage());
            throw AUTH_ERROR;
        } catch (IllegalStateException var7) {
            this.logger.debug("IllegalStateException thrown while decoding JWT token " + var7.getLocalizedMessage());
            throw AUTH_ERROR;
        } catch (SignatureException var8) {
            this.logger.debug("SignatureException thrown while decoding JWT token " + var8.getLocalizedMessage());
            throw AUTH_ERROR;
        } catch (IOException var9) {
            this.logger.debug("IOException thrown while decoding JWT token " + var9.getLocalizedMessage());
            throw AUTH_ERROR;
        }
    }

    public boolean supports(Class<?> authentication) {
        return Auth0JWTToken.class.isAssignableFrom(authentication);
    }

    public void afterPropertiesSet() throws Exception {
        if(this.clientSecret != null && this.clientId != null) {
            if(this.securedRoute == null) {
                throw new RuntimeException("You must set which route pattern is used to check for users so that they must be authenticated");
            } else {
                this.jwtVerifier = new JWTVerifier(this.clientSecret, this.clientId);
            }
        } else {
            throw new RuntimeException("client secret and client id are not set for Auth0AuthenticationProvider");
        }
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
}
class tokenrequest
{
    private String id_token;

    public tokenrequest(String id_token)
    {

        this.id_token = id_token;
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }
}
