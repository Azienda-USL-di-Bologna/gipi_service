package it.bologna.ausl.gipi.security.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.filter.GenericFilterBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtFilter extends GenericFilterBean {

    private final String secretKey;
    private final UserDetailsService userDetailsService;
    private final AuthorizationUtils authorizationUtils;

    public JwtFilter(String secretKey, UserDetailsService userDetailsService, AuthorizationUtils authorizationUtils) {
        super();
        this.secretKey = secretKey;
        this.userDetailsService = userDetailsService;
        this.authorizationUtils = authorizationUtils;
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
            
            Claims claims = authorizationUtils.setInSecurityContext(token, secretKey);
            request.setAttribute("claims", claims);
        } catch (final SignatureException e) {
            throw new ServletException("Invalid token.");
        }

        chain.doFilter(req, res);
    }
}
