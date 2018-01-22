package it.bologna.ausl.gipi.security.jwt;

import it.bologna.ausl.entities.baborg.Utente;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * Created by fan.jin on 2016-11-11.
 */
public class TokenBasedAuthentication extends AbstractAuthenticationToken {

    private String token;
//    private final UserInfo userInfo;
    private final UserDetails principal;

    public TokenBasedAuthentication(UserInfo userInfo, UserDetails principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        super.setDetails(userInfo);
    }

    public String getToken() {
        return token;
    }

    public void setToken( String token ) {
        this.token = token;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }

}
