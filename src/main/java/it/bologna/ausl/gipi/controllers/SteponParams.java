package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.entities.gipi.Iter;
import java.util.Date;

public class SteponParams {

    private int idIter;
    private String dataPassaggio;
    private String codiceRegistroDocumento;
    private String numeroDocumento;
    private Integer annoDocumento;
    private String notePassaggio;
    private String esito;
    private String motivazioneEsito;
    private String oggettoDocumento;

    public int getIdIter() {
        return idIter;
    }

    public void setIter(int idIter) {
        this.idIter = idIter;
    }

    public String getDataPassaggio() {
        return dataPassaggio;
    }

    public void setDataPassaggio(String dataPassaggio) {
        this.dataPassaggio = dataPassaggio;
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

    public String getNotePassaggio() {
        return notePassaggio;
    }

    public void setNotePassaggio(String notePassaggio) {
        this.notePassaggio = notePassaggio;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }

    public String getMotivazioneEsito() {
        return motivazioneEsito;
    }

    public void setMotivazioneEsito(String motivazioneEsito) {
        this.motivazioneEsito = motivazioneEsito;
    }

    public String getOggettoDocumento() {
        return oggettoDocumento;
    }

    public void setOggettoDocumento(String oggettoDocumento) {
        this.oggettoDocumento = oggettoDocumento;
    }
}
