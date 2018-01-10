package it.bologna.ausl.gipi.service;

import it.bologna.ausl.entities.baborg.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UtenteRepository extends CrudRepository<Utente, Integer> {

    public Utente findByUsername(@Param("username") String username);

//    public Utente getByCodiceFiscale(@Param("codiceFiscale") String codiceFiscale);
}
