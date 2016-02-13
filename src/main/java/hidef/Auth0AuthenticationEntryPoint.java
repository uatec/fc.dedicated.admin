//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package hidef;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class Auth0AuthenticationEntryPoint implements AuthenticationEntryPoint {
    public Auth0AuthenticationEntryPoint() {
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        PrintWriter writer = response.getWriter();
        if(this.isPreflight(request)) {
            response.setStatus(204);
        } else if(authException instanceof Auth0TokenException) {
            response.setStatus(401, authException.getMessage());
            writer.println("HTTP Status 401 - " + authException.getMessage());
        } else {
            response.setStatus(403, authException.getMessage());
            writer.println("HTTP Status 403 - " + authException.getMessage());
        }

    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }
}
