package hidef;

import com.mashape.unirest.http.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Auth0UserDetails implements UserDetails {
    private static final long serialVersionUID = 2058797193125711681L;
    private Map<String, Object> details;
    private JSONObject jsonDetails;
    private String username;
    private boolean emailVerified = false;
    private Collection<GrantedAuthority> authorities = null;
    private static final Log LOGGER = LogFactory.getLog(Auth0UserDetails.class);

    public Auth0UserDetails(JSONObject map)
    {
        this.username = map.optString("email",
                map.optString("username",
                        map.optString("user_id", "UNKNOWN_USER")));

        this.emailVerified = Boolean.valueOf(map.optString("email_verified", "false"));

        this.authorities = new ArrayList();
        if(map.optJSONArray("roles") != null) {
            JSONArray roles = null;

            try {
                roles = map.getJSONArray("roles");

                for ( int i = 0; i < roles.length(); i++ )
                {
                    String role = roles.getString(i);
                    this.authorities.add(new SimpleGrantedAuthority(role));
                }
            } catch (ClassCastException var5) {
                var5.printStackTrace();
                LOGGER.error("Error in casting the roles object");
            }
        }

        if(this.authorities.isEmpty()) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.jsonDetails = map;
    }

    public Auth0UserDetails(Map<String, Object> map) {
        if(map.containsKey("email")) {
            this.username = map.get("email").toString();
        } else if(map.containsKey("username")) {
            this.username = map.get("username").toString();
        } else if(map.containsKey("user_id")) {
            this.username = map.get("user_id").toString();
        } else {
            this.username = "UNKOWNUN_USER";
        }

        if(map.containsKey("email")) {
            this.emailVerified = Boolean.valueOf(map.get("email_verified").toString()).booleanValue();
        }

        this.authorities = new ArrayList();
        if(map.containsKey("roles")) {
            ArrayList roles = null;

            try {
                roles = (ArrayList)map.get("roles");
                Iterator e = roles.iterator();

                while(e.hasNext()) {
                    String role = (String)e.next();
                    this.authorities.add(new SimpleGrantedAuthority(role));
                }
            } catch (ClassCastException var5) {
                var5.printStackTrace();
                LOGGER.error("Error in casting the roles object");
            }
        }

        if(this.authorities.isEmpty()) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.details = map;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public String getPassword() {
        throw new UnsupportedOperationException("Password is protected");
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isAccountNonExpired() {
        return false;
    }

    public boolean isAccountNonLocked() {
        return false;
    }

    public boolean isCredentialsNonExpired() {
        return false;
    }

    public boolean isEnabled() {
        return this.emailVerified;
    }

    public Object getAuth0Attribute(String attributeName) {
        if ( this.details != null ) {
            return this.details.get(attributeName);
        } else {
            return this.jsonDetails.get(attributeName);
        }
    }
}
