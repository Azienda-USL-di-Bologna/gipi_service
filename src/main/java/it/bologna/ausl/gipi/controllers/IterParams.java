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
    
  private int idUtente;
  private int idStrutturaUtente;
  private int idProcedimento;
  private int idAzienda;
  private String oggettoIter;
  private Date dataCreazioneIter;
  private Date dataAvvioIter;
  private String codiceRegistroDocumento;
  private int numeroDocumento;
  private int annoDocumento;
  

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public int getIdStrutturaUtente() {
        return idStrutturaUtente;
    }

    public void setIdStrutturaUtente(int idStrutturaUtente) {
        this.idStrutturaUtente = idStrutturaUtente;
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

    public int getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(int numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public int getAnnoDocumento() {
        return annoDocumento;
    }

    public void setAnnoDocumento(int annoDocumento) {
        this.annoDocumento = annoDocumento;
    }
    
}
