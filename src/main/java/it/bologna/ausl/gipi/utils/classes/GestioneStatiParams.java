package it.bologna.ausl.gipi.utils.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author utente
 */
public class GestioneStatiParams {

    public String statoRichiesto;
    public int idIter;
    public String cfAutore;
    public int idAzienda;
    public String codiceRegistroDocumento;
    public String numeroDocumento;
    public int annoDocumento;
    public String oggettoDocumento;
    public Date dataEvento;
//    public Date sospesoDal;
//    @Temporal(TemporalType.DATE)
//    public Date sospesoAl;
//    @Temporal(TemporalType.DATE)
    public String note;
    public String nomeProcedimento;
    public String esito;
    public String esitoMotivazione;
    public String azione;
    public String idOggettoOrigine;
    public String tipoOggettoOrigine;
    public String descrizione;
    public String idApplicazione;
    public String glogParams;

    public String getGlogParams() {
        return glogParams;
    }

    public String getStatoRichiesto() {
        return statoRichiesto;
    }

    public void setStatoRichiesto(String statoRichiesto) {
        this.statoRichiesto = statoRichiesto;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }

    public String getEsitoMotivazione() {
        return esitoMotivazione;
    }

    public void setEsitoMotivazione(String esitoMotivazione) {
        this.esitoMotivazione = esitoMotivazione;
    }

    public int getIdIter() {
        return idIter;
    }

    public void setIdIter(int idIter) {
        this.idIter = idIter;
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

    public String getOggettoDocumento() {
        return oggettoDocumento;
    }

    public void setOggettoDocumento(String oggettoDocumento) {
        this.oggettoDocumento = oggettoDocumento;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNomeProcedimento() {
        return nomeProcedimento;
    }

    public void setNomeProcedimento(String nomeProcedimento) {
        this.nomeProcedimento = nomeProcedimento;
    }

    public Date getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(Date dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getAzione() {
        return azione;
    }

    public void setAzione(String azione) {
        this.azione = azione;
    }

    public String getIdOggettoOrigine() {
        return idOggettoOrigine;
    }

    public void setIdOggettoOrigine(String idOggettoOrigine) {
        this.idOggettoOrigine = idOggettoOrigine;
    }

    public String getTipoOggettoOrigine() {
        return tipoOggettoOrigine;
    }

    public void setTipoOggettoOrigine(String tipoOggettoOrigine) {
        this.tipoOggettoOrigine = tipoOggettoOrigine;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCfAutore() {
        return cfAutore;
    }

    public void setCfAutore(String cfAutore) {
        this.cfAutore = cfAutore;
    }

    public int getIdAzienda() {
        return idAzienda;
    }

    public void setIdAzienda(int idAzienda) {
        this.idAzienda = idAzienda;
    }

    public String getIdApplicazione() {
        return idApplicazione;
    }

    public void setIdApplicazione(String idApplicazione) {
        this.idApplicazione = idApplicazione;
    }
    
    public boolean isDifferito() {
        return getAzione().equals("associazione_differita") || getAzione().equals("cambio_di_stato_differito");
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
