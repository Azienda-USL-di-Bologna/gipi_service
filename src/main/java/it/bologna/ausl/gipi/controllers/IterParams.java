/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

//import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author f.gusella
 */
public class IterParams {
//    @JsonProperty(value = "FK_id_responsabile_procedimento")
//    private int FK_id_responsabile_procedimento;
    
    private int idUtenteResponsabile;
    private int idUtenteLoggato;
    private int idProcedimento;
    private int idAzienda;
    private String oggettoIter;
    private Date dataCreazioneIter;
    private Date dataAvvioIter;
    private String codiceRegistroDocumento;
    private String numeroDocumento;
    private int annoDocumento;
//    private int idIter;
//    private String cfResponsabileProcedimento;
//    private int annoIter;
//    private String nomeProcedimento;

    public int getIdUtenteResponsabile() {
        return idUtenteResponsabile;
    }

    public void setIdUtenteResponsabile(int idUtenteResponsabile) {
        this.idUtenteResponsabile = idUtenteResponsabile;
    }

    public int getIdUtenteLoggato() {
        return idUtenteLoggato;
    }

    public void setIdUtenteLoggato(int idUtenteLoggato) {
        this.idUtenteLoggato = idUtenteLoggato;
    }

    public int getIdProcedimento() {
        return idProcedimento;
    }

    public void setIdProcedimento(int idProcedimento) {
        this.idProcedimento = idProcedimento;
    }

    public int getIdAzienda() {
        return idAzienda;
    }

    public void setIdAzienda(int idAzienda) {
        this.idAzienda = idAzienda;
    }

    public String getOggettoIter() {
        return oggettoIter;
    }

    public void setOggettoIter(String oggettoIter) {
        this.oggettoIter = oggettoIter;
    }

    public Date getDataCreazioneIter() {
        return dataCreazioneIter;
    }

    public void setDataCreazioneIter(Date dataCreazioneIter) {
        this.dataCreazioneIter = dataCreazioneIter;
    }

    public Date getDataAvvioIter() {
        return dataAvvioIter;
    }

    public void setDataAvvioIter(Date dataAvvioIter) {
        this.dataAvvioIter = dataAvvioIter;
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

//    public int getIdIter() {
//        return idIter;
//    }
//
//    public void setIdIter(int idIter) {
//        this.idIter = idIter;
//    }
//
//    public String getCfResponsabileProcedimento() {
//        return cfResponsabileProcedimento;
//    }
//
//    public void setCfResponsabileProcedimento(String cfResponsabileProcedimento) {
//        this.cfResponsabileProcedimento = cfResponsabileProcedimento;
//    }
//
//    public int getAnnoIter() {
//        return annoIter;
//    }
//
//    public void setAnnoIter(int annoIter) {
//        this.annoIter = annoIter;
//    }
//
//    public String getNomeProcedimento() {
//        return nomeProcedimento;
//    }
//
//    public void setNomeProcedimento(String nomeProcedimento) {
//        this.nomeProcedimento = nomeProcedimento;
//    }
      
    @JsonIgnore
    public String getJSONString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        String writeValueAsString = mapper.writeValueAsString(this);
        return writeValueAsString;
    }
}
