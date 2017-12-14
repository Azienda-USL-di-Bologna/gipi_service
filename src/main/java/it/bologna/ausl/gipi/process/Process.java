/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.process;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QFase;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author user
 */
@Component
public class Process {

    QFase qFase = QFase.fase;
    QFaseIter qFaseIter = QFaseIter.faseIter;
    QIter qIter = QIter.iter;

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

    public Fase getCurrentFase(Iter iter) {
        QFase fase = QFase.fase;

        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Fase currentFase = query
                .from(fase)
                .where(fase.id.eq(iter.getIdFase().getId()))
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
    public void stepOn(Iter iter, SteponParams params) {

        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Iter iterRicaricato = queryIter
                .from(qIter)
                .where(qIter.id.eq(iter.getId()))
                .fetchOne();

        // INSERIMENTO NUOVA FASE-ITER
        Fase nextFase = getNextFase(iterRicaricato);

        FaseIter faseIterToInsert = new FaseIter();
        faseIterToInsert.setDataInizioFase(new Date());
        faseIterToInsert.setIdFase(nextFase);
        faseIterToInsert.setIdIter(iterRicaricato);
        em.persist(faseIterToInsert);

        // AGGIORNA VECCHIA FASE ITER
        Fase currentFase = getCurrentFase(iterRicaricato);

        JPQLQuery<FaseIter> queryFaseIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        FaseIter currentFaseIter = queryFaseIter
                .from(qFaseIter)
                .where(qFaseIter.idFase.id.eq(currentFase.getId())
                        .and(qFaseIter.idIter.id.eq(iterRicaricato.getId())))
                .fetchOne();
        currentFaseIter.setDataFineFase(new Date());

        // AGGIORNA CAMPI SU ITER
        iterRicaricato.setIdFase(nextFase);
        em.persist(iterRicaricato);

        // INSERIMENTO EVENTO
        // inserisci documento Iter
    }

//    public String getSteponParam(String key) {
//
//        String value = steponParams.get(key);
//
//        if (value == null) {
//            throw new Exception("nessun parametro corrispondente alla chiave: " + key);
//        }
//        return value;
//    }
//
//    public String putSteponParam(String key, String value) {
//        steponParams.put(key, value);
//    }
}
