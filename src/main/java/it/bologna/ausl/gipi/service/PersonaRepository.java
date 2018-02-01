package it.bologna.ausl.gipi.service;

import it.bologna.ausl.entities.baborg.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    public Persona getByCodiceFiscale(@Param("codiceFiscale") String codiceFiscale);
}
