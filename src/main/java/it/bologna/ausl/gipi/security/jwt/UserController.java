package it.bologna.ausl.gipi.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.AziendaParametriJson;
import it.bologna.ausl.entities.baborg.Persona;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.QPersona;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.gipi.utils.PasswordHash;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.utilities.UtilityFunctions;
import it.bologna.ausl.gipi.security.auth.TokenBasedAuthentication;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gipi/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

//    @Value("${jwt.saml.user.field:CodiceFiscale}")
//    private String samlUser;
//
//    @Value("${jwt.saml.db.login_field:cf}")
//    private String dbField;
    @Value("${jwt.saml.company-identification-field:companyName}")
    private String companyIdentificationField;

    @Value("${jwt.saml.enabled:false}")
    private boolean samlEnabled;

    @Autowired
    CustomUserDetailsService userDb;

    @Autowired
    CustomUserInfoService userInfoService;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    UtilityFunctions utilityFunctions;

    public UserController() {
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponse> loginPOST(@RequestBody final UserLogin userLogin) throws ServletException, NoSuchAlgorithmException, InvalidKeySpecException {
        UserDetails ud = null;

        logger.debug("login username: " + userLogin.username);

        // considera username
        ud = userDb.loadUserByUsername(userLogin.username);
        if (userLogin.username == null || ud == null) {
            throw new ServletException("Invalid login");
        }

        // considera password
        if (userLogin.password == null || ud == null) {
            throw new ServletException("Invalid login");
        }

        if (!PasswordHash.validatePassword(userLogin.password, ud.getPassword())) {
            throw new ServletException("Invalid login");
        }

        String token = Jwts.builder()
                .setSubject(ud.getUsername())
                .claim("roles", "admin")
                .setIssuedAt(new Date())
                .signWith(SIGNATURE_ALGORITHM, SECRET_KEY)
                .compact();

        TokenBasedAuthentication authentication = new TokenBasedAuthentication(ud);
        authentication.setToken(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new ResponseEntity(
                new LoginResponse(
                token,
                ud.getUsername(),
                new HashMap()),
                HttpStatus.OK);
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ResponseEntity<LoginResponse> loginGET(HttpServletRequest request) throws ServletException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, ClassNotFoundException {

        //LOGIN SAML
        if (!samlEnabled) {
            return new ResponseEntity("SAML authentication not enabled", HttpStatus.UNAUTHORIZED);
        }
        Utente user;
//        String codiceAziendaConRegione = request.getAttribute(this.companyIdentificationField).toString();
        String codiceAziendaConRegione = "080105";

        JPQLQuery<Azienda> queryAzienda = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Azienda azienda = queryAzienda
                .from(QAzienda.azienda)
                .where(QAzienda.azienda.codiceRegione.append(QAzienda.azienda.codice).eq(codiceAziendaConRegione))
                .fetchOne();

        AziendaParametriJson aziendaParams = AziendaParametriJson.parse(objectMapper, azienda.getParametri());

//        String ssoField = request.getAttribute(aziendaParams.getLoginSSOField()).toString();
        String ssoFieldValue = "DMRGPP83E29D851C";

        String[] loginDbFieldSplitted = aziendaParams.getLoginDBField().split("/");
        String entityClassName = loginDbFieldSplitted[0];
        String field = loginDbFieldSplitted[1];

        Class<?> entityClass = Class.forName(entityClassName);

        if (entityClass.isAssignableFrom(Persona.class)) {
            PathBuilder<Utente> qUtente = new PathBuilder(Utente.class, "utente");
            PathBuilder<Persona> qPersona = qUtente.get("idPersona", Persona.class);
            JPQLQuery<Utente> queryUser = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            user = queryUser
                    .from(QUtente.utente).
                    where(qPersona.get(field).eq(ssoFieldValue).
                            and(QUtente.utente.idAzienda.codice.eq(azienda.getCodice())))
                    .fetchOne();


        } else if (entityClass.isAssignableFrom(Utente.class)) {
            JPQLQuery<Utente> queryUser = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            PathBuilder<Utente> qUtente = new PathBuilder(Utente.class, "utente");
            user = queryUser
                    .from(QUtente.utente).
                    where(qUtente.get(field).eq(ssoFieldValue).
                            and(QUtente.utente.idAzienda.codice.eq(azienda.getCodice())))
                    .fetchOne();

            System.out.println("single result: " + user.toString());
        } else {
            throw new ServletException(String.format("field %s invalid", field));
        }

//        ud = userDb.loadByParameter(dbField, user);
//        ud = userDb.getUtenteTemp(ssoFieldValue);
        if (user == null) {
            throw new ServletException("User not found");
        }
        
        
        
        System.out.println("autorities:   " + Arrays.toString(user.getAuthorities().toArray()));
        
//        Utente userCasted = (Utente) user;
        user.setAuthorities(utilityFunctions.buildAuthorities(user, em));
        user.getUtenteStrutturaList();
        
        //logger.info(String.format("User: %s logged in %s ", ud.getUsername(), ((Utente) ud).getDescrizione()));
        return new ResponseEntity(
                new LoginResponse(
                Jwts.builder().setSubject(user.getUsername())
                    .claim("user", user)
                    .setIssuedAt(new Date())
                    .signWith(SIGNATURE_ALGORITHM, SECRET_KEY).compact(),
                user.getUsername(),
                userInfoService.loadUserInfoMap(user, azienda)), 
                HttpStatus.OK);
//        return new ResponseEntity(new LoginResponse(Jwts.builder().setSubject(user.getUsername())
//                .claim("roles", "admin").setIssuedAt(new Date())
//                .signWith(SIGNATURE_ALGORITHM, SECRET_KEY).compact(),
//                user.getUsername(),
//                userInfoService.loadUserInfoMapByUsername(user.getUsername())), HttpStatus.OK);
    }

    @SuppressWarnings("unused")
    public static class UserLogin {

        public String username;
        public String password;
    }

    @SuppressWarnings("unused")
    public static class LoginResponse {

        public String token;
        public String username;
        public Map userInfo;

        public LoginResponse(final String token, final String username, Map userInfo) {
            this.token = token;
            this.username = username;
            this.userInfo = userInfo;
        }
    }
}
