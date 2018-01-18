package it.bologna.ausl.gipi.security.jwt;


import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.gipi.service.UtenteRepository;
import it.bologna.ausl.entities.baborg.Utente;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service 
public class CustomUserInfoService{

    @Autowired
    private UtenteRepository utenteRepository;
    private Map<String, Object> userInfo;
    
    
    public Map loadUserInfoMap(Utente utente, Azienda aziendaLogin) throws UsernameNotFoundException {
//        Utente utenteCaricato = utenteRepository.findByUsername(username);

        this.userInfo = new HashMap<>();
        userInfo.put("username", utente.getUsername());
        
        userInfo.put("idUtente", utente.getId());
        
        //Ruolo
        userInfo.put("bit_ruoli_utente", utente.getBitRuoli());
        
    //Ruolo
        userInfo.put("bit_ruoli_persona", utente.getIdPersona().getBitRuoli());

        //Azienda
//        for (UtenteAzienda utenteAzienda: utenteCaricato.getUtenteAziendaList()) {
//            utenteAzienda.getIdAzienda();
//        }

        List<Azienda> aziende = new ArrayList();
        utente.getIdPersona().getUtenteList().stream().forEach(
            u -> {
                aziende.add(u.getIdAzienda());
            }
        );
        
        userInfo.put("aziende", aziende);
        
        userInfo.put("azienda_login", aziendaLogin);
        
        //List<UtenteStruttura>
        userInfo.put("strutture", utente.getUtenteStrutturaList());
        
        userInfo.put("ruoli", utente.getAuthorities());
        
        
        return userInfo;
    }
    
}
