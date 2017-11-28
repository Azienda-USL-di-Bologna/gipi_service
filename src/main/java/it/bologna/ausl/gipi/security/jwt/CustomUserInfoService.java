/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.security.jwt;


import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.gipi.service.UtenteRepository;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.baborg.UtenteStruttura;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
        this.userInfo = new HashMap<String, Object>();
        userInfo.put("username", utenteCaricato.getUsername());
        
        //Ruolo
        userInfo.put("ruolo", utenteCaricato.getIdRuolo());

        //Azienda
        userInfo.put("azienda", utenteCaricato.getIdAzienda());
        
        //List<UtenteStruttura>
        userInfo.put("strutture", utenteCaricato.getUtenteStrutturaList());
        }
        
        return userInfo;
    }
    
}
