package it.bologna.ausl.gipi.service;

import it.bologna.ausl.entities.baborg.Persona;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends CrudRepository<Persona, Integer> {

    public Persona getByCodiceFiscale(@Param("codiceFiscale") String codiceFiscale);
}
