/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.process;

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
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    EntityManager em;

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
    public void stepOn(Iter iter, ProcessSteponParams processParams) throws ParseException, GipiRequestParamsException {

        System.out.println("CHE NON RIESCO A SENTIRTI");
        System.out.println("iter" + iter);
        System.out.println("Params");
        processParams.getParams().forEach((key, value) -> {
            System.out.println("Key : " + key + " Value : " + value);
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
        String esito = (String) processParams.readParam("esito");
        String motivazioneEsito = (String) processParams.readParam("motivazioneEsito");

        if(nextFase.getFaseDiChiusura())
            iter.setStato("chiuso");
            
        if (!nextFase.getFaseDiChiusura() && (esito != null || motivazioneEsito != null)) {
            System.out.println("Qui lancio l'eccezione perchè la fase non è di chiusura e gli arriva esito o motivazioneEsito");
            throw new GipiRequestParamsException("i campi esito e motivazioneEsito sono previsti solo se la nextFase è di chiusura");
            // THROW
        }
        iter.setEsito(esito);
        iter.setEsitoMotivazione(motivazioneEsito);

        em.persist(iter); // questo salva anche la dataFineFase sulla fase appena finita

        // inserisci DOCUMENTO-ITER
        DocumentoIter documentoIter = new DocumentoIter();
        documentoIter.setRegistro((String) processParams.readParam("codiceRegistroDocumento"));
        documentoIter.setAnno((Integer) processParams.readParam("annoDocumento"));
        documentoIter.setNumeroRegistro((String) processParams.readParam("numeroDocumento"));
        documentoIter.setIdIter(iter);
        em.persist(documentoIter);
//
        // INSERIMENTO EVENTO
        // mi recupero l'evento.
        JPQLQuery<Evento> queryEvento = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Evento evento = queryEvento
                .from(qEvento)
                .where(qEvento.codice.eq("passaggio_fase"))
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
    }
}
