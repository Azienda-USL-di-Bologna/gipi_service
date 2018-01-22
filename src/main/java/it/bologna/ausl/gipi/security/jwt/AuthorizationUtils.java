package it.bologna.ausl.gipi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.gipi.service.UtenteRepository;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author gdm
 */
@Component
public class AuthorizationUtils {
    @Autowired
    UtenteRepository utenteRepository;
    @Autowired
    EntityManager em;

    /**
     * inserisce nel securityContext l'utente inserito nel token al momento del login
     * @param token il token
     * @param secretKey la chiave segreta per decifrare il token
     * @return i claims del token
     */
    public Claims setInSecurityContext(String token, String secretKey) {
        Claims claims = Jwts.parser().
            setSigningKey(secretKey).
            parseClaimsJws(token).
            getBody();

        String idUtente = claims.getSubject();
//            objectMapper.
//            String writeValueAsString = objectMapper.writeValueAsString(claims.get("user"));
//            Utente utente = objectMapper.readerWithView(View.Authorization.class)
//                .forType(Utente.class)
//                .readValue(writeValueAsString);
//            UserInfo userInfo = objectMapper.convertValue(claims.get("userInfo"), UserInfo.class);
        //UserDetails user = userDetailsService.loadUserByUsername(username);
//        Utente utente = new Utente(userInfo.getIdUtente());
//        utente.setAuthorities(Arrays.asList(
//                new SimpleGrantedAuthority(String.valueOf(userInfo.getBitRuoliPersona())),
//                new SimpleGrantedAuthority(String.valueOf(userInfo.getBitRuoliUtente()))));
        Utente utente = utenteRepository.findOne(Integer.parseInt(idUtente));
        UserInfo userInfo = UserInfo.loadUserInfo(utente, utente.getIdAzienda(), em);
        TokenBasedAuthentication authentication = new TokenBasedAuthentication(userInfo, utente);
        authentication.setToken(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return claims;
    }
}
