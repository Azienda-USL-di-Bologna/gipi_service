package it.bologna.ausl.gipi.security.jwt;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.QRuolo;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.baborg.UtenteStruttura;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 *
 * @author gdm
 */
public class UserInfo {
    
private Integer idUtente;
private String username;
private Integer bitRuoliUtente;
private Integer bitRuoliPersona;
private List<Azienda> aziende;
private Azienda aziendaLogin;
private List<UtenteStruttura> strutture;
private List<Ruolo> ruoli;

    public UserInfo() {
    }

    public UserInfo(Integer idUtente, String username, Integer bitRuoliUtente, Integer bitRuoliPersona, List<Azienda> aziende, Azienda aziendaLogin, List<UtenteStruttura> strutture, List<Ruolo> ruoli) {
        this.idUtente = idUtente;
        this.username = username;
        this.bitRuoliUtente = bitRuoliUtente;
        this.bitRuoliPersona = bitRuoliPersona;
        this.aziende = aziende;
        this.aziendaLogin = aziendaLogin;
        this.strutture = strutture;
        this.ruoli = ruoli;
    }

    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getBitRuoliUtente() {
        return bitRuoliUtente;
    }

    public void setBitRuoliUtente(Integer bitRuoliUtente) {
        this.bitRuoliUtente = bitRuoliUtente;
    }

    public Integer getBitRuoliPersona() {
        return bitRuoliPersona;
    }

    public void setBitRuoliPersona(Integer bitRuoliPersona) {
        this.bitRuoliPersona = bitRuoliPersona;
    }

    public List<Azienda> getAziende() {
        return aziende;
    }

    public void setAziende(List<Azienda> aziende) {
        this.aziende = aziende;
    }

    public Azienda getAziendaLogin() {
        return aziendaLogin;
    }

    public void setAziendaLogin(Azienda aziendaLogin) {
        this.aziendaLogin = aziendaLogin;
    }

    public List<UtenteStruttura> getStrutture() {
        return strutture;
    }

    public void setStrutture(List<UtenteStruttura> strutture) {
        this.strutture = strutture;
    }

    public List<Ruolo> getRuoli() {
        return ruoli;
    }

    public void setRuoli(List<Ruolo> ruoli) {
        this.ruoli = ruoli;
    }

    public static UserInfo loadUserInfo(Utente utente, Azienda aziendaLogin, EntityManager em) throws UsernameNotFoundException {

    UserInfo userInfo = new UserInfo();
    userInfo.setIdUtente(utente.getId());
    userInfo.setUsername(utente.getUsername());
    userInfo.setBitRuoliUtente(utente.getBitRuoli());
    userInfo.setBitRuoliPersona(utente.getIdPersona().getBitRuoli());
    
    List<Azienda> aziende = new ArrayList<>();
    utente.getIdPersona().getUtenteList().stream().forEach(
        u -> {
            aziende.add(u.getIdAzienda());
        }
    );
    userInfo.setAziende(aziende);
    userInfo.setAziendaLogin(aziendaLogin);
    userInfo.setStrutture(utente.getUtenteStrutturaList());
    userInfo.setRuoli(getRuoli(utente, em));
    
    return userInfo;
    }

    /**
     * calcola tutti i ruoli dell'utente, compresi quelli derivanti dalla persona
     * @param utente
     * @param em
     * @return la lista dei ruoli calcolati
     */
    public static List<Ruolo> getRuoli(Utente utente, EntityManager em) {
        List<Ruolo> res = new ArrayList<>();
        JPQLQuery<Ruolo> queryRuoli = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        List<Ruolo> ruoliAll = queryRuoli
                .from(QRuolo.ruolo).fetchResults().getResults();
        
        // System.out.println("ruoli: " + Arrays.toString(ruoliAll.toArray()));
        
        for (Ruolo ruolo : ruoliAll) {
            if (ruolo.getSuperAziendale()) {
                if ((utente.getIdPersona().getBitRuoli() & ruolo.getMascheraBit()) > 0) {
                    res.add(ruolo);
                }
            }
            else {
                if ((utente.getBitRuoli() & ruolo.getMascheraBit()) > 0) {
                    res.add(ruolo);
                }
            }
        }
        return res;
    }
}
