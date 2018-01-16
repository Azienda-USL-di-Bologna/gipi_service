package it.bologna.ausl.gipi.security.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.filter.GenericFilterBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import it.bologna.ausl.gipi.security.auth.TokenBasedAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

public class JwtFilter extends GenericFilterBean {

    private String SECRET_KEY;
    
    private UserDetailsService userDetailsService;

    public JwtFilter(String secretKey, UserDetailsService userDetailsService) {
        super();
        this.SECRET_KEY = secretKey;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(final ServletRequest req,
            final ServletResponse res,
            final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServletException("Missing or invalid Authorization header.");
        }

        // la parte dopo "Bearer "
        final String token = authHeader.substring(7);

        try {
            final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token).getBody();
//            HttpServletRequest httpReq = (HttpServletRequest) req;
//            if (httpReq.getRequestURI().equalsIgnoreCase(authHeader))
            String username = claims.getSubject();
            UserDetails user = userDetailsService.loadUserByUsername(username);
            
            TokenBasedAuthentication authentication = new TokenBasedAuthentication(user);
            authentication.setToken(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            request.setAttribute("claims", claims);
        } catch (final SignatureException e) {
            throw new ServletException("Invalid token.");
        }

        chain.doFilter(req, res);
    }
}
