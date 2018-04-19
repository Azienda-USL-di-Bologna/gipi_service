package it.bologna.ausl.gipi.utils;

import it.bologna.ausl.gipi.utils.classes.GestioneStatiParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.gipi.Iter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import com.google.gson.JsonObject;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.controllers.IterController;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author l.s.
 */
@Component
public class GestioneStatoIter {

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${babelGestisciIter}")
    private String babelGestisciIterPath;

    @Value("${updateGdDoc}")
    private String updateGdDocPath;

    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;

    @Autowired
    private IterController iterController;

    QIter qIter = QIter.iter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    QEvento qEvento = QEvento.evento;
    QAzienda qAzienda = QAzienda.azienda;

    public ResponseEntity gestisciStatoIter(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException {
        if (gestioneStatiParams.getNumeroDocumento().equals("")) {
            return gestisciStatoIterDaBozza(gestioneStatiParams);
        }

        Utente u = GetEntityById.getUtenteFromPersonaByCodiceFiscaleAndIdAzineda(gestioneStatiParams.getCfAutore(), gestioneStatiParams.getIdAzienda(), em);
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);
        //Evento e = GetEntityById.getEventoByCodice(gestioneStatiParams.getStatoRichiesto().getCodice().toString(), em);
        Evento eventoDiCambioStato = new Evento();
        FaseIter fi = iterController.getFaseIter(i);
        Stato s = GetEntityById.getStatoByCodice(gestioneStatiParams.getStatoRichiesto(), em);

        if (s.getCodice().equals(i.getIdStato().getCodice())) // qui siamo se stiamo solo aggiungendo un documento
        {
            eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("aggiunta_documento");
        } else {
            // Questa non è una cosa bellissima e bisognerebbe fare un refactoring anche di questo
            // Infatti non abbiamo un modo automatico per determinare l'Evento in base allo Stato, nè abbiamo un enum sugli eventi
            if (s.getCodice().equals(Stato.CodiciStato.SOSPESO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("apertura_sospensione");
            } else if (s.getCodice().equals(Stato.CodiciStato.CHIUSO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_iter");
                i.setEsito(gestioneStatiParams.getEsito());
                i.setEsitoMotivazione(gestioneStatiParams.getEsitoMotivazione());
            } else {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_sospensione");
            }

            // Aggiorno l'iter
            i.setIdStato(s);
        }

        em.persist(i);
        // Creo il documento iter
        DocumentoIter d = new DocumentoIter();
        d.setAnno(gestioneStatiParams.getAnnoDocumento());
        d.setIdIter(i);
        d.setNumeroRegistro(gestioneStatiParams.getNumeroDocumento());
        d.setRegistro(gestioneStatiParams.getCodiceRegistroDocumento());
        d.setOggetto(gestioneStatiParams.getOggettoDocumento());
        em.persist(d);
        em.flush();

        // Creo l'evento iter
        EventoIter ei = new EventoIter();
        ei.setIdDocumentoIter(d);
        ei.setNote(gestioneStatiParams.getNote());
        ei.setIdEvento(eventoDiCambioStato);
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setDataOraEvento(gestioneStatiParams.getDataEvento());
        ei.setIdFaseIter(fi);
        em.persist(ei);

        // Fascicolo il documento 
        String baseUrl = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);

        // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        String urlChiamata = urlChiamata = baseUrl + updateGdDocPath;
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, (String) gestioneStatiParams.getCodiceRegistroDocumento(), null, (String) gestioneStatiParams.getNumeroDocumento(), null, null, null, null, null, null, null, (Integer) gestioneStatiParams.getAnnoDocumento());
        Fascicolazione fascicolazione = new Fascicolazione(i.getIdFascicolo(), null, null, null, DateTime.now(), Document.DocumentOperationType.INSERT);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        okhttp3.RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                .build();
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response responseg = client.newCall(requestg).execute();
        if (!responseg.isSuccessful()) {
            throw new IOException("La fascicolazione non è andata a buon fine.");
        }

        // Comunico a Babel l'associazione documento/iter appena avvenuta
        urlChiamata = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper) + babelGestisciIterPath;
        //String baseUrl = "http://gdml:8080" + baseUrlBabelGestisciIter;
//        gestioneStatiParams.setCfResponsabileProcedimento(i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
//        gestioneStatiParams.setAnnoIter(i.getAnno());
//        gestioneStatiParams.setNomeProcedimento(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());

        JsonObject o = new JsonObject();
        o.addProperty("idIter", i.getId());
        o.addProperty("numeroIter", i.getNumero());
        o.addProperty("annoIter", i.getAnno());
        o.addProperty("cfResponsabileProcedimento", i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        o.addProperty("nomeProcedimento", i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        o.addProperty("codiceRegistroDocumento", gestioneStatiParams.getCodiceRegistroDocumento());
        o.addProperty("numeroDocumento", gestioneStatiParams.getNumeroDocumento());
        o.addProperty("annoDocumento", gestioneStatiParams.getAnnoDocumento());
        o.addProperty("idOggettoOrigine", gestioneStatiParams.getIdOggettoOrigine());

        body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

        requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "associaDocumento")
                .post(body)
                .build();

        client = new OkHttpClient();
        responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a Babel non è andata a buon fine.");
        }

        JsonObject obj = new JsonObject();
        o.addProperty("idIter", i.getId().toString());

        return new ResponseEntity(obj.toString(), HttpStatus.OK);
    }

    public ResponseEntity gestisciStatoIterDaBozza(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException {
        // Tutto quello che mi serve lo si ricava da GestioneStatiParams o nell'iter (che si ricava da GestioneStatiParams)

        // Recupero l'iter
        Iter i = GetEntityById.getIter(gestioneStatiParams.getIdIter(), em);

        // Setto i dati_differiti (sono quelli da usare alla numerazione del documento)
        JsonObject dati_differiti = new JsonObject();
        dati_differiti.addProperty("cfAutore", gestioneStatiParams.getCfAutore());
        dati_differiti.addProperty("idAzienda", gestioneStatiParams.getIdAzienda());
        dati_differiti.addProperty("azione", gestioneStatiParams.getAzione());
        dati_differiti.addProperty("statoRichiesto", gestioneStatiParams.getStatoRichiesto());
        dati_differiti.addProperty("note", gestioneStatiParams.getNote());
        dati_differiti.addProperty("esito", gestioneStatiParams.getEsito());
        dati_differiti.addProperty("esitoMotivazione", gestioneStatiParams.getEsitoMotivazione());

        // Preparo l'oggetto da passare alla web api di associazione documento
        JsonObject o = new JsonObject();
        o.addProperty("idIter", i.getId());
        o.addProperty("numeroIter", i.getNumero());
        o.addProperty("annoIter", i.getAnno());
        o.addProperty("cfResponsabileProcedimento", i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        o.addProperty("nomeProcedimento", i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        o.addProperty("idOggettoOrigine", gestioneStatiParams.getIdOggettoOrigine());
        o.addProperty("numeroDocumento", gestioneStatiParams.getNumeroDocumento());
        o.addProperty("annoDocumento", gestioneStatiParams.getAnnoDocumento());
        o.addProperty("codiceRegistroDocumento", gestioneStatiParams.getCodiceRegistroDocumento());
        o.addProperty("datiDifferiti", dati_differiti.toString());

        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        System.out.println(o.toString());

        // Chiamata alla web api GestisciIter.associaDocumento
        String urlChiamata = GetBaseUrl.getBaseUrl(gestioneStatiParams.getIdAzienda(), em, objectMapper) + babelGestisciIterPath;

        // DA CANCELLARE!!!!
        urlChiamata = "http://127.0.0.1:8080/Babel/GestisciIter";

        System.out.println(urlChiamata);
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "associaDocumento")
                .post(body)
                .build();

        System.out.println(requestg.toString());
        OkHttpClient client = new OkHttpClient();
        Response responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            System.out.println(responseg.toString());
            throw new IOException("La chiamata a Babel non è andata a buon fine. " + responseg.toString());
        }
        // ritorno un oggetto di ok
        JsonObject responseAndObj = new JsonObject();
        responseAndObj.addProperty("response", "TUTTO E' ANDATO BENISSIMO");
        responseAndObj.addProperty("object", o.toString());
        return new ResponseEntity(responseAndObj.toString(), HttpStatus.OK);
    }
}
