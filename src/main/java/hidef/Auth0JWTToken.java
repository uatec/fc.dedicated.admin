package hidef;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class Auth0JWTToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 2371882820082543721L;
    private final String jwt;
    private Auth0UserDetails principal;

    public Auth0JWTToken(String jwt) {
        super(null);
        this.jwt = jwt;
        this.setAuthenticated(false);
    }

    public String getJwt() {
        return this.jwt;
    }

    public Object getCredentials() {
        return null;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(Auth0UserDetails principal) {
        this.principal = principal;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.principal.getAuthorities();
    }
}
