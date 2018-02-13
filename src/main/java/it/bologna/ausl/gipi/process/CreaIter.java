/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.ProcedimentoCache;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QFase;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.gipi.controllers.IterParams;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import it.bologna.ausl.gipi.utils.GetEntityById;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolo;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.persistence.EntityManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author f.gusella
 */
@Component
@PropertySource("classpath:application.properties") 
public class CreaIter {

    QFase qFase = QFase.fase;
    QEvento qEvento = QEvento.evento;
    QIter qIter = QIter.iter;
    QAzienda qAzienda = QAzienda.azienda;
    
    private static final String EVENTO_CREAZIONE_ITER = "avvio_iter";
    private static final String STATO_INIZIALE_ITER = "in_corso";
    
    @Value("${insertFascicolo}")
    private String baseUrlBdsInsertFascicolo;
    
    @Value("${updateGdDoc}")
    private String baseUrlBdsUpdateGdDoc;
    
    @Value("${babelAvviaIter}")
    private String baseUrlBabelAvviaIter;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;

    public Fase getFaseIniziale(int idAzienda) {
        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Fase f = query
                .from(this.qFase)
                .where(this.qFase.idAzienda.id.eq(idAzienda)
                        .and(this.qFase.ordinale.eq(1)))
                .fetchFirst();
        return f;
    }

    public Evento getEventoCreazioneIter() {
        JPQLQuery<Evento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Evento e = query
                .from(this.qEvento)
                .where(this.qEvento.codice.eq(EVENTO_CREAZIONE_ITER))
                .fetchFirst();
        return e;
    }

    public int getNumeroIterMax() {
        JPQLQuery<Iter> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
//        Long maxId = query(this.qIter.numeroIter.max());
        int i = query.select(this.qIter.numero.max())
                .from(this.qIter).fetchFirst();
//                .unique(this.qIter.numeroIter.max())
//                .fetchFirst();
//        JPQLQuery<Integer> a = JPAExpressions.select(this.qIter.numeroIter.max())
//                  .from(this.qIter);
        return i;
    }
    
    public Iter creaIter(IterParams iterParams) throws IOException {

        // Mi carico i dati di cui ho bisogno per creare l'iter.
        Procedimento p = GetEntityById.getProcedimento(iterParams.getIdProcedimento(), em);
        Utente uLoggato = GetEntityById.getUtente(iterParams.getIdUtenteLoggato(), em);
        Utente uResponsabile = GetEntityById.getUtente(iterParams.getIdUtenteResponsabile(), em);
        Fase f = this.getFaseIniziale(iterParams.getIdAzienda());
        Evento e = this.getEventoCreazioneIter();
        // Sistemo il numero documento che ho in iterParams deve avere 7 cifre, se non le ha, aggiungo degli zeri da sinistra
        iterParams.setNumeroDocumento(String.format("%07d", Integer.parseInt(iterParams.getNumeroDocumento())));
        
        // *********************************************
        // Buildo l'iter
        Iter i = new Iter();
        i.setIdFaseCorrente(f);
        i.setIdProcedimento(p);
        i.setIdResponsabileProcedimento(uResponsabile);
        i.setNumero(getNumeroIterMax() + 1);
        i.setAnno(Calendar.getInstance().get(Calendar.YEAR));
        i.setOggetto(iterParams.getOggettoIter());
        i.setStato(STATO_INIZIALE_ITER);
        i.setDataCreazione(iterParams.getDataCreazioneIter());
        i.setDataAvvio(iterParams.getDataAvvioIter());
        // i.setIdFascicolo(fascicolo.getNumerazioneGerarchica());
        i.setNomeFascicolo(iterParams.getOggettoIter());
        i.setIdTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo());
        i.setNomeTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo().getNome());
        em.persist(i);
        em.flush();
        
        
        // *********************************************
        // Creo il fascicolo dell'iter.
        Fascicolo fascicolo = new Fascicolo(null, iterParams.getOggettoIter(), null, null, new DateTime(), 0,
                Calendar.getInstance().get(Calendar.YEAR), "1", null, new DateTime(), null, null, "a",
                0, null, -1, null, null, uLoggato.getIdPersona().getCodiceFiscale(), uResponsabile.getIdPersona().getCodiceFiscale(), null,
                p.getIdAziendaTipoProcedimento().getIdTitolo().getClassificazione(), i.getId());
        fascicolo.setIdTipoFascicolo(2);
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo);
        // String url = "https://gdml.internal.ausl.bologna.it/bds_tools/InsertFascicolo";             // Questo va spostato e reso parametrico
        String baseUrl = GetBaseUrl.getBaseUrl(iterParams.getIdAzienda(), em, objectMapper) + baseUrlBdsInsertFascicolo;
        
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String resString = response.body().string();
        fascicolo = (Fascicolo) it.bologna.ausl.ioda.iodaobjectlibrary.Requestable.parse(resString, Fascicolo.class);
        
        // Aggiungo la numerazione gerarchica del fascicolo all'iter
        i.setIdFascicolo(fascicolo.getNumerazioneGerarchica());
        em.persist(i);
        
        // *********************************************
        // Fascicolo il documento // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        baseUrl = GetBaseUrl.getBaseUrl(iterParams.getIdAzienda(), em, objectMapper) + baseUrlBdsUpdateGdDoc;
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, iterParams.getCodiceRegistroDocumento(), null, iterParams.getNumeroDocumento(), null, null, null, null, null, null, null, iterParams.getAnnoDocumento());
        Fascicolazione fascicolazione = new Fascicolazione(fascicolo.getNumerazioneGerarchica(), fascicolo.getNomeFascicolo(), fascicolo.getIdUtenteCreazione(), null, DateTime.now(), Document.DocumentOperationType.INSERT);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        RequestBody bodyg = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                    .build();
        Request requestg = new Request.Builder()
                .url(baseUrl)
                .post(bodyg)
                .build();
        Response responseg = client.newCall(requestg).execute();
        if (!responseg.isSuccessful()) {
            throw new IOException("La fascicolazione non è andata a buon fine.");
        }
        
        // *********************************************
        // Costruisco gli altri vari oggetti connessi all'iter
        // Buildo il Procedimento Cache
        ProcedimentoCache pc = new ProcedimentoCache();
        pc.setId(i.getId());                                // ?? Ma qui non dovrei passere l'iter intero e non solo l'id?? forse manca fk?
        pc.setNomeTipoProcedimento(p.getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        pc.setDescrizioneTipoProcedimento(p.getIdAziendaTipoProcedimento().getDescrizioneTipoProcedimento());
        pc.setIdStruttura(p.getIdStruttura());
        pc.setIdTitolarePotereSostitutivo(p.getIdTitolarePotereSostitutivo());
        pc.setDurataMassimaProcedimento(p.getIdAziendaTipoProcedimento().getDurataMassimaProcedimento());
        pc.setDurataMassimaSospensione(p.getIdAziendaTipoProcedimento().getDurataMassimaSospensione());
        em.persist(pc);

        // Buildo la fase Iter
        FaseIter fi = new FaseIter();
        fi.setIdIter(i);
        fi.setIdFase(f);
        fi.setDataInizioFase(i.getDataAvvio());
        em.persist(fi);

        // Buildo il documento
        DocumentoIter di = new DocumentoIter();
        di.setIdIter(i);
        di.setNumeroRegistro(String.valueOf(iterParams.getNumeroDocumento()));
        di.setAnno(iterParams.getAnnoDocumento());
        di.setRegistro(iterParams.getCodiceRegistroDocumento());
        em.persist(di);

        // Buildo l'evento Iter
        EventoIter ei = new EventoIter();
        ei.setIdEvento(e);
        ei.setIdIter(i);
        ei.setIdFaseIter(fi);
        ei.setDataOraEvento(iterParams.getDataCreazioneIter());
        ei.setIdDocumentoIter(di);
        ei.setAutore(uLoggato);
        em.persist(ei);
        
        // Comunico a Babel l'iter appena creato
        baseUrl = GetBaseUrl.getBaseUrl(iterParams.getIdAzienda(), em, objectMapper) + baseUrlBabelAvviaIter;
        
        iterParams.setIdIter(i.getId());
        iterParams.setCfResponsabileProcedimento(uResponsabile.getIdPersona().getCodiceFiscale());
        iterParams.setAnnoIter(i.getAnno());
        iterParams.setNomeProcedimento(p.getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        
        body = RequestBody.create(JSON, iterParams.getJSONString().getBytes("UTF-8"));
        
        requestg = new Request.Builder()
                .url(baseUrl)
                .addHeader("X-HTTP-Method-Override", "associaDocumento")
                .post(body)
                .build();
        
        client = new OkHttpClient();
        responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a Babel non è andata a buon fine.");
        }

        return i;
    }
}
