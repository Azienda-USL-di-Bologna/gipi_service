package it.bologna.ausl.gipi.process;

import it.bologna.ausl.entities.gipi.Fase;

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

}
