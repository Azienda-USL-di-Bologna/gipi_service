/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.pubblicazioni;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.bologna.ausl.ioda.iodaobjectlibrary.Requestable;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */
public class RegistroAccessi implements Marshaller{
    
    private Integer id;
    private String oggetto;
    private Integer annoPubblicazione;
    private Integer numeroPubblicazione;
    private String tipoProcedimento;
    private String modalitaCollegamento;
    private String uoProcedente;
    private String responsabileProcedimento;
    private String codiceRegistroIniziativa;
    private String registroIniziativa;
    private String numeroRegistroIniziativa;
    private int annoRegistroIniziativa;
    private String dataIniziativa;
    private String codiceRegistroChiusura;
    private String registroChiusura;
    private String numeroRegistroChiusura;
    private int annoRegistroChiusura;
    private String dataChiusura;
    private boolean controinteressati;
    private String esito;
    private String sintesiMotivazioneRifuto;
    private LocalDateTime dataInserimentoRiga;
    private LocalDateTime dataUltimaModificaRiga;
    private Azienda idAzienda;

    public RegistroAccessi() {
    }

    public RegistroAccessi(Integer id, String oggetto, Integer annoPubblicazione, Integer numeroPubblicazione, String tipoProcedimento, String modalitaCollegamento, String uoProcedente, String responsabileProcedimento, String codiceRegistroIniziativa, String registroIniziativa, String numeroRegistroIniziativa, int annoRegistroIniziativa, String dataIniziativa, String codiceRegistroChiusura, String registroChiusura, String numeroRegistroChiusura, int annoRegistroChiusura, String dataChiusura, boolean controinteressati, String esito, String sintesiMotivazioneRifuto, LocalDateTime dataInserimentoRiga, LocalDateTime dataUltimaModificaRiga, Azienda idAzienda) {
        this.id = id;
        this.oggetto = oggetto;
        this.annoPubblicazione = annoPubblicazione;
        this.numeroPubblicazione = numeroPubblicazione;
        this.tipoProcedimento = tipoProcedimento;
        this.modalitaCollegamento = modalitaCollegamento;
        this.uoProcedente = uoProcedente;
        this.responsabileProcedimento = responsabileProcedimento;
        this.codiceRegistroIniziativa = codiceRegistroIniziativa;
        this.registroIniziativa = registroIniziativa;
        this.numeroRegistroIniziativa = numeroRegistroIniziativa;
        this.annoRegistroIniziativa = annoRegistroIniziativa;
        this.dataIniziativa = dataIniziativa;
        this.codiceRegistroChiusura = codiceRegistroChiusura;
        this.registroChiusura = registroChiusura;
        this.numeroRegistroChiusura = numeroRegistroChiusura;
        this.annoRegistroChiusura = annoRegistroChiusura;
        this.dataChiusura = dataChiusura;
        this.controinteressati = controinteressati;
        this.esito = esito;
        this.sintesiMotivazioneRifuto = sintesiMotivazioneRifuto;
        this.dataInserimentoRiga = dataInserimentoRiga;
        this.dataUltimaModificaRiga = dataUltimaModificaRiga;
        this.idAzienda = idAzienda;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public Integer getAnnoPubblicazione() {
        return annoPubblicazione;
    }

    public void setAnnoPubblicazione(Integer annoPubblicazione) {
        this.annoPubblicazione = annoPubblicazione;
    }

    public Integer getNumeroPubblicazione() {
        return numeroPubblicazione;
    }

    public void setNumeroPubblicazione(Integer numeroPubblicazione) {
        this.numeroPubblicazione = numeroPubblicazione;
    }

    public String getTipoProcedimento() {
        return tipoProcedimento;
    }

    public void setTipoProcedimento(String tipoProcedimento) {
        this.tipoProcedimento = tipoProcedimento;
    }

    public String getModalitaCollegamento() {
        return modalitaCollegamento;
    }

    public void setModalitaCollegamento(String modalitaCollegamento) {
        this.modalitaCollegamento = modalitaCollegamento;
    }

    public String getUoProcedente() {
        return uoProcedente;
    }

    public void setUoProcedente(String uoProcedente) {
        this.uoProcedente = uoProcedente;
    }

    public String getResponsabileProcedimento() {
        return responsabileProcedimento;
    }

    public void setResponsabileProcedimento(String responsabileProcedimento) {
        this.responsabileProcedimento = responsabileProcedimento;
    }

    public String getCodiceRegistroIniziativa() {
        return codiceRegistroIniziativa;
    }

    public void setCodiceRegistroIniziativa(String codiceRegistroIniziativa) {
        this.codiceRegistroIniziativa = codiceRegistroIniziativa;
    }

    public String getRegistroIniziativa() {
        return registroIniziativa;
    }

    public void setRegistroIniziativa(String registroIniziativa) {
        this.registroIniziativa = registroIniziativa;
    }

    public String getNumeroRegistroIniziativa() {
        return numeroRegistroIniziativa;
    }

    public void setNumeroRegistroIniziativa(String numeroRegistroIniziativa) {
        this.numeroRegistroIniziativa = numeroRegistroIniziativa;
    }

    public int getAnnoRegistroIniziativa() {
        return annoRegistroIniziativa;
    }

    public void setAnnoRegistroIniziativa(int annoRegistroIniziativa) {
        this.annoRegistroIniziativa = annoRegistroIniziativa;
    }

    public String getDataIniziativa() {
        return dataIniziativa;
    }

    public void setDataIniziativa(String dataIniziativa) {
        this.dataIniziativa = dataIniziativa;
    }

    public String getCodiceRegistroChiusura() {
        return codiceRegistroChiusura;
    }

    public void setCodiceRegistroChiusura(String codiceRegistroChiusura) {
        this.codiceRegistroChiusura = codiceRegistroChiusura;
    }

    public String getRegistroChiusura() {
        return registroChiusura;
    }

    public void setRegistroChiusura(String registroChiusura) {
        this.registroChiusura = registroChiusura;
    }

    public String getNumeroRegistroChiusura() {
        return numeroRegistroChiusura;
    }

    public void setNumeroRegistroChiusura(String numeroRegistroChiusura) {
        this.numeroRegistroChiusura = numeroRegistroChiusura;
    }

    public int getAnnoRegistroChiusura() {
        return annoRegistroChiusura;
    }

    public void setAnnoRegistroChiusura(int annoRegistroChiusura) {
        this.annoRegistroChiusura = annoRegistroChiusura;
    }

    public String getDataChiusura() {
        return dataChiusura;
    }

    public void setDataChiusura(String dataChiusura) {
        this.dataChiusura = dataChiusura;
    }

    public boolean getControinteressati() {
        return controinteressati;
    }

    public void setControinteressati(boolean controinteressati) {
        this.controinteressati = controinteressati;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }

    public String getSintesiMotivazioneRifuto() {
        return sintesiMotivazioneRifuto;
    }

    public void setSintesiMotivazioneRifuto(String sintesiMotivazioneRifuto) {
        this.sintesiMotivazioneRifuto = sintesiMotivazioneRifuto;
    }

    public LocalDateTime getDataInserimentoRiga() {
        return dataInserimentoRiga;
    }

    public void setDataInserimentoRiga(LocalDateTime dataInserimentoRiga) {
        this.dataInserimentoRiga = dataInserimentoRiga;
    }

    public LocalDateTime getDataUltimaModificaRiga() {
        return dataUltimaModificaRiga;
    }

    public void setDataUltimaModificaRiga(LocalDateTime dataUltimaModificaRiga) {
        this.dataUltimaModificaRiga = dataUltimaModificaRiga;
    }

    public Azienda getIdAzienda() {
        return idAzienda;
    }

    public void setIdAzienda(Azienda idAzienda) {
        this.idAzienda = idAzienda;
    }  
    
//    @JsonIgnore
//    public String getJSONString() throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());
//        mapper.setTimeZone(TimeZone.getDefault());
//        String writeValueAsString = mapper.writeValueAsString(this);
//        return writeValueAsString;
//    }  
//    
//    @JsonIgnore
//    public static RegistroAccessi parse(String value) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());
//        mapper.setTimeZone(TimeZone.getDefault());
//        return mapper.readValue(value, RegistroAccessi.class);
//    }
//
//    @JsonIgnore
//    public static RegistroAccessi parse(InputStream value) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());
//        mapper.setTimeZone(TimeZone.getDefault());
//        return mapper.readValue(value, RegistroAccessi.class);
//    }
    
}
