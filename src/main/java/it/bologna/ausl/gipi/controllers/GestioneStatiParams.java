/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.bologna.ausl.entities.gipi.Stato;
import java.util.Date;
import java.util.TimeZone;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author utente
 */
public class GestioneStatiParams {
    public int idStato;
    public int idIter;
    public int idUtente;
    public String codiceRegistroDocumento;
    public String numeroDocumento;
    public int annoDocumento;
    public Date dataEvento;
//    public Date sospesoDal;
//    @Temporal(TemporalType.DATE)
//    public Date sospesoAl;
//    @Temporal(TemporalType.DATE)
    public String note;
//    public int idAzienda;
    public int annoIter;
    public String cfResponsabileProcedimento;
    public String nomeProcedimento;

    public int getIdIter() {
        return idIter;
    }

    public void setIdIter(int idIter) {
        this.idIter = idIter;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }


    public String getCodiceRegistroDocumento() {
        return codiceRegistroDocumento;
    }

    public void setCodiceRegistroDocumento(String codiceRegistroDocumento) {
        this.codiceRegistroDocumento = codiceRegistroDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public int getAnnoDocumento() {
        return annoDocumento;
    }

    public void setAnnoDocumento(int annoDocumento) {
        this.annoDocumento = annoDocumento;
    }

//    public Date getSospesoDal() {
//        return sospesoDal;
//    }
//
//    public void setSospesoDal(Date sospesoDal) {
//        this.sospesoDal = sospesoDal;
//    }
//
//    public Date getSospesoAl() {
//        return sospesoAl;
//    }
//
//    public void setSospesoAl(Date sospesoAl) {
//        this.sospesoAl = sospesoAl;
//    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

//    public int getIdAzienda() {
//        return idAzienda;
//    }
//
//    public void setIdAzienda(int idAzienda) {
//        this.idAzienda = idAzienda;
//    }
//
    public int getAnnoIter() {
        return annoIter;
    }

    public void setAnnoIter(int annoIter) {
        this.annoIter = annoIter;
    }

    public String getCfResponsabileProcedimento() {
        return cfResponsabileProcedimento;
    }

    public void setCfResponsabileProcedimento(String cfResponsabileProcedimento) {
        this.cfResponsabileProcedimento = cfResponsabileProcedimento;
    }

    public String getNomeProcedimento() {
        return nomeProcedimento;
    }

    public void setNomeProcedimento(String nomeProcedimento) {
        this.nomeProcedimento = nomeProcedimento;
    }

    public int getStato() {
        return idStato;
    }

    public Date getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(Date dataEvento) {
        this.dataEvento = dataEvento;
    }
    
    
    @JsonIgnore
    public String getJSONString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        String writeValueAsString = mapper.writeValueAsString(this);
        return writeValueAsString;
    }
}
