package it.bologna.ausl.gipi.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class RegistrationBean {

    @Value("${jwt.security.registration.bean.node}")
    private String ROOT_NAME;
    @Value("${jwt.secret:secret}")
    private String SECRET_KEY;
    
    @Autowired
    UserDetailsService userDetailsService;

    @Bean
    public FilterRegistrationBean jwtFilter() {

        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        registrationBean.setFilter(new JwtFilter(SECRET_KEY, userDetailsService));

        // intercetta tutte le chiamate che iniziano per...
        registrationBean.addUrlPatterns(ROOT_NAME);

        return registrationBean;
    }
}
