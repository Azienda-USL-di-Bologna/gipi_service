package it.nextsw.gipi.service;

import it.nextsw.entities.organigramma.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by user on 14/06/2017.
 */
public interface UtenteRepository extends CrudRepository<Utente, Integer> {

    public Utente findByUsername(@Param("username") String username);
}
