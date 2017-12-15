package it.bologna.ausl.gipi.process;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QFase;

@Co
public class ProcessStatus {

    Fase currentFase;
    Fase nextFase;

    public ProcessStatus() {
    }

    public ProcessStatus(Fase currentFase, Fase nextFase) {
        this.currentFase = currentFase;
        this.nextFase = nextFase;
    }

    public Fase getCurrentFase() {
        return currentFase;
    }

    public void setCurrentFase(Fase currentFase) {
        this.currentFase = currentFase;
    }

    public Fase getNextFase() {
        return nextFase;
    }

    public void setNextFase(Fase nextFase) {
        this.nextFase = nextFase;
    }

    public void build(Iter iter) {

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

}
