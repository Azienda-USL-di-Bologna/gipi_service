/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.bologna.ausl.entities.gipi.Iter;
import java.util.Date;

/**
 *
 * @author f.gusella
 */
public class IterParams {
    
    private Date dataCreazione;
    private Date dataAvvio;
    private String oggetto;
    @JsonProperty(value = "FK_id_responsabile_procedimento")
    private int FK_id_responsabile_procedimento;
    private int id;

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public Date getDataAvvio() {
        return dataAvvio;
    }

    public void setDataAvvio(Date dataAvvio) {
        this.dataAvvio = dataAvvio;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public int getFK_id_responsabile_procedimento() {
        return FK_id_responsabile_procedimento;
    }

    public void setFK_id_responsabile_procedimento(int FK_id_responsabile_procedimento) {
        this.FK_id_responsabile_procedimento = FK_id_responsabile_procedimento;
    }

    
  

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
