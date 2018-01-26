/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

//import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

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
}
