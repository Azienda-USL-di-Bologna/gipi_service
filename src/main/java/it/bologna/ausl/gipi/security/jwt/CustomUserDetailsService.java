package it.bologna.ausl.gipi.security.jwt;

import it.bologna.ausl.entities.baborg.Persona;
import it.bologna.ausl.gipi.service.UtenteRepository;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.gipi.service.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private PersonaRepository personaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utente user = utenteRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user;
        }
    }

//    public UserDetails loadByParameter(String field, String value) {
//        return utenteRepository.getByCodiceFiscale(value);
//    }

    /**
     * temporaneamente torniamo il primo utente associato alla persona con il codice fiscale passato
     * TODO: l'utente andrà selezionato in base all'azienda con dalla quale l'utente fa il login
     * @param codFisc
     * @return 
     */
    public UserDetails getUtenteTemp(String codFisc) {
        Persona persona = personaRepository.getByCodiceFiscale(codFisc);
        return persona.getUtenteList().get(0);
    }
}
