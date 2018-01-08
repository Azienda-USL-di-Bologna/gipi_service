package it.bologna.ausl.gipi.security.jwt;


import it.bologna.ausl.gipi.service.UtenteRepository;
import it.bologna.ausl.entities.baborg.Utente;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service 
public class CustomUserInfoService{

    @Autowired
    private UtenteRepository utenteRepository;
    private Map<String, Object> userInfo;
    
    
    public Map loadUserInfoMapByUsername(String username) throws UsernameNotFoundException {
        Utente utenteCaricato = utenteRepository.findByUsername(username);
        
        if (utenteCaricato == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {     
        this.userInfo = new HashMap<>();
        userInfo.put("username", utenteCaricato.getUsername());
        
        userInfo.put("idUtente", utenteCaricato.getId());
        
        //Ruolo
        userInfo.put("ruoloAziendale", utenteCaricato.getIdRuoloAziendale());

        //Azienda
//        for (UtenteAzienda utenteAzienda: utenteCaricato.getUtenteAziendaList()) {
//            utenteAzienda.getIdAzienda();
//        }
        userInfo.put("aziende", utenteCaricato.getIdAzienda());
        
        //List<UtenteStruttura>
        userInfo.put("strutture", utenteCaricato.getUtenteStrutturaList());
        }
        
        return userInfo;
    }
    
}
