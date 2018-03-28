/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QFase;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.QStato;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.EntityManager;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author user
 */
@Component
public class Process {

    QFaseIter qFaseIter = QFaseIter.faseIter;
    QIter qIter = QIter.iter;
    QEvento qEvento = QEvento.evento;
    QUtente qUtente = QUtente.utente;
    QFase qFase = QFase.fase;
    QStato qStato = QStato.stato;

    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;
    
    @Value("${babelsuite.uri.localhost}")
    private String localhostBaseUrl;
    
    @Value("${updateGdDoc}")
    private String updateGdDocPath;
    
    @Value("${babelGestisciIter}")
    private String babelGestisciIterPath;
    
    @Autowired
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;
    
    private static final Logger logger = Logger.getLogger(CreaIter.class);

    public Fase getNextFase(Iter iter) {

        Fase currentFase = getCurrentFase(iter);

        QFase fase = QFase.fase;

        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Fase nextFase = query
                .from(fase)
                .where(fase.ordinale.gt(currentFase.getOrdinale())
                        .and(fase.idAzienda.id.eq(currentFase.getIdAzienda().getId())))
                .orderBy(fase.ordinale.asc())
                .fetchFirst();

        System.out.println("nextFase " + nextFase);
        return nextFase;
    }

    public Fase getFasi(Iter iter) {

        Fase currentFase = getCurrentFase(iter);

        QFase fase = QFase.fase;

        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Fase nextFase = query
                .from(fase)
                .where(fase.ordinale.gt(currentFase.getOrdinale())
                        .and(fase.idAzienda.id.eq(currentFase.getIdAzienda().getId())))
                .orderBy(fase.ordinale.asc())
                .fetchFirst();

        System.out.println("nextFase " + nextFase);
        return nextFase;
    }

    public Fase getCurrentFase(Iter iter) {

        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Fase currentFase = query
                .from(qFase)
                .where(qFase.id.eq(iter.getIdFaseCorrente().getId()))
                .fetchOne();

//        QIter iter = QIter.iter;
//        QFaseIter faseIter = QFaseIter.faseIter;
        // metodo complesso: non funziona
        // volevo trovare la fase partendo dall'iter senza usare la fk idFaseCorrente
//        Fase faseReturned = query
//                .from(fase)
//                .join(faseIter).on(faseIter.idFase.id.eq(fase.id))
//                .join(iter).on(faseIter.idIter.id.eq(iter.id))
//                .where(iter.id.eq(selectedIter.getId()))
//                .orderBy(fase.ordinale.desc())
//                .fetchFirst();
        System.out.println("currentFase " + currentFase);
        return currentFase;

    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void stepOn(Iter iter, ProcessSteponParams processParams, boolean isLocalHost) throws ParseException, GipiRequestParamsException, IOException {
        logger.info("iter" + iter);
        logger.info("Params");
        processParams.getParams().forEach((key, value) -> {
            logger.info("Key : " + key + " Value : " + value);
        });

        // INSERIMENTO NUOVA FASE-ITER
        Fase nextFase = getNextFase(iter);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dataPassaggio = format.parse((String) processParams.readParam("dataPassaggio"));
        FaseIter faseIterToInsert = new FaseIter();
        //faseIterToInsert.setDataInizioFase(new Date());
        faseIterToInsert.setDataInizioFase(dataPassaggio);
        faseIterToInsert.setIdFase(nextFase);
        faseIterToInsert.setIdIter(iter);
        em.persist(faseIterToInsert);

        // AGGIORNA VECCHIA FASE ITER
        Fase currentFase = getCurrentFase(iter);

        JPQLQuery<FaseIter> queryFaseIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        FaseIter currentFaseIter = queryFaseIter
                .from(qFaseIter)
                .where(qFaseIter.idFase.id.eq(currentFase.getId())
                        .and(qFaseIter.idIter.id.eq(iter.getId())))
                .fetchOne();
        currentFaseIter.setDataFineFase(dataPassaggio);

        // AGGIORNA CAMPI SU ITER
        iter.setIdFaseCorrente(nextFase);

        if(nextFase.getFaseDiChiusura()){
            iter.setIdStato(entitiesCachableUtilities.loadStatoByCodice(Stato.CodiciStato.CHIUSO));
            iter.setEsito((String) processParams.readParam("esito"));
            iter.setEsitoMotivazione((String) processParams.readParam("motivazioneEsito"));
        }
        
        em.persist(iter); // questo salva anche la dataFineFase sulla fase appena finita
        
        // inserisci DOCUMENTO-ITER
        DocumentoIter documentoIter = new DocumentoIter();
        documentoIter.setRegistro((String) processParams.readParam("codiceRegistroDocumento"));
        documentoIter.setAnno((Integer) processParams.readParam("annoDocumento"));
        documentoIter.setNumeroRegistro((String) processParams.readParam("numeroDocumento"));
        documentoIter.setIdIter(iter);
        documentoIter.setOggetto((String) processParams.readParam("oggettoDocumento"));
        em.persist(documentoIter);
        

//
        // INSERIMENTO EVENTO
        // mi recupero l'evento.
        JPQLQuery<Evento> queryEvento = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Evento evento = queryEvento
                .from(qEvento)
                .where(qEvento.codice.eq(nextFase.getFaseDiChiusura() ? "chiusura_iter" : "passaggio_fase"))
                .fetchOne();
        
        // Mi prendo l'idUtente loggato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Integer idUtenteLoggato = (Integer) userInfo.get(UtenteCachable.KEYS.ID);

        JPQLQuery<Utente> queryUtente = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Utente utente = queryUtente
                .from(qUtente)
                .where(qUtente.id.eq(idUtenteLoggato))
                .fetchOne();

        EventoIter eventoIter = new EventoIter();
        eventoIter.setIdEvento(evento);
        eventoIter.setIdIter(iter);
        eventoIter.setIdFaseIter(currentFaseIter);
        eventoIter.setIdDocumentoIter(documentoIter);
        eventoIter.setAutore(utente);
        eventoIter.setNote((String) processParams.readParam("notePassaggio"));
        eventoIter.setDataOraEvento(dataPassaggio);
        em.persist(eventoIter);
        
        // Fascicolo il documento 
        String baseUrl;
        if (isLocalHost)
            baseUrl = localhostBaseUrl;
        else
            baseUrl = GetBaseUrl.getBaseUrl(iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        
        // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        String urlChiamata = urlChiamata = baseUrl + updateGdDocPath;
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, (String) processParams.readParam("codiceRegistroDocumento"), null, (String) processParams.readParam("numeroDocumento"), null, null, null, null, null, null, null, (Integer) processParams.readParam("annoDocumento"));
        Fascicolazione fascicolazione = new Fascicolazione(iter.getIdFascicolo(), null, null, null, DateTime.now(), Document.DocumentOperationType.INSERT);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        RequestBody body = new MultipartBody.Builder()
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
        
        // Comunico a Babel l'iter appena creato
        urlChiamata = baseUrl + babelGestisciIterPath;
         
        JsonObject o = new JsonObject();
        o.addProperty("idIter", iter.getId());
        o.addProperty("numeroIter", iter.getNumero());
        o.addProperty("annoIter", iter.getAnno());
        o.addProperty("cfResponsabileProcedimento", iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        o.addProperty("nomeProcedimento", iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        o.addProperty("codiceRegistroDocumento", (String) processParams.readParam("codiceRegistroDocumento"));
        o.addProperty("numeroDocumento", (String) processParams.readParam("numeroDocumento"));
        o.addProperty("annoDocumento", (Integer) processParams.readParam("annoDocumento"));
               
        body = RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        
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
        
    }
}
