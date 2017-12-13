package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.entities.gipi.Iter;
import java.util.Date;

public class SteponParams {

    private String idIter;
    private String dataPassaggio;
    private String documento;
    private String notePassaggio;
    private String esito;
    private String motivazioneEsito;

    public String getIdIter() {
        return idIter;
    }

    public void setIter(String idIter) {
        this.idIter = idIter;
    }

    public String getDataPassaggio() {
        return dataPassaggio;
    }

    public void setDataPassaggio(String dataPassaggio) {
        this.dataPassaggio = dataPassaggio;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
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

}
