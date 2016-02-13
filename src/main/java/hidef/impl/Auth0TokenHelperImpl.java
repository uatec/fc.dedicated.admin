package hidef.impl;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.ClaimSet;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JwtSigner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hidef.Auth0TokenHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public class Auth0TokenHelperImpl implements Auth0TokenHelper<Object>, InitializingBean {
    private static final Log Logger = LogFactory.getLog(Auth0TokenHelperImpl.class);
    private String clientSecret = null;
    private String clientId = null;

    public Auth0TokenHelperImpl() {
    }

    public String generateToken(Object object, int expiration) {
        try {
            JwtSigner e = new JwtSigner();
            String payload = (new ObjectMapper()).writeValueAsString(object);
            ClaimSet claimSet = new ClaimSet();
            claimSet.setExp(expiration);
            String token = e.encode(Algorithm.HS256, payload, "payload", new String(Base64.decodeBase64(this.clientSecret)), claimSet);
            return token;
        } catch (JsonProcessingException var7) {
            throw new Auth0RuntimeException(var7);
        } catch (Exception var8) {
            throw new Auth0RuntimeException(var8);
        }
    }

    public Object decodeToken(String token) {
        JWTVerifier jwtVerifier = new JWTVerifier(this.clientSecret, this.clientId);

        try {
            Map verify = jwtVerifier.verify(token);
            String e = (String)verify.get("$");
            return new ObjectMapper().readValue(e, Map.class);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IllegalStateException | IOException var6) {
            throw new Auth0RuntimeException(var6);
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.clientSecret, "The client secret is not set for " + this.getClass());
        Assert.notNull(this.clientId, "The client id is not set for " + this.getClass());
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
