package hidef;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class Auth0AuthenticationFilter extends GenericFilterBean {
    @Autowired
    private AuthenticationManager authenticationManager;
    private AuthenticationEntryPoint entryPoint;

    public Auth0AuthenticationFilter() {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        if(request.getMethod().equals("OPTIONS")) {
            chain.doFilter(request, response);
        } else {
            String jwt = this.getToken(request);
            if(jwt != null) {
                try {
                    Auth0JWTToken failed = new Auth0JWTToken(jwt);
                    Authentication authResult = this.authenticationManager.authenticate(failed);
                    SecurityContextHolder.getContext().setAuthentication(authResult);
                } catch (AuthenticationException var9) {
                    SecurityContextHolder.clearContext();
                    this.entryPoint.commence(request, response, var9);
                    return;
                }
            }

            chain.doFilter(request, response);
        }
    }

    private String getToken(HttpServletRequest httpRequest) {
        String token = null;
        String authorizationHeader = httpRequest.getHeader("authorization");
        if(authorizationHeader == null) {
            return null;
        } else {
            String[] parts = authorizationHeader.split(" ");
            if(parts.length != 2) {
                return null;
            } else {
                String scheme = parts[0];
                String credentials = parts[1];
                Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
                if(pattern.matcher(scheme).matches()) {
                    token = credentials;
                }

                return token;
            }
        }
    }

    public AuthenticationEntryPoint getEntryPoint() {
        return this.entryPoint;
    }

    public void setEntryPoint(AuthenticationEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }
}
