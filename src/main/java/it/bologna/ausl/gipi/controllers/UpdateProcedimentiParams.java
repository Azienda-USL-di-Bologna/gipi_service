package it.bologna.ausl.gipi.controllers;

import java.util.Map;

public class UpdateProcedimentiParams {

    public static enum Operations {
        INSERT, DELETE;
    }

    private Integer idAziendaTipoProcedimento;
    private Map<Integer, Operations> nodeInvolved;

    public UpdateProcedimentiParams() {
    }

    public Integer getIdAziendaTipoProcedimento() {
        return idAziendaTipoProcedimento;
    }

    public void setIdAziendaTipoProcedimento(Integer idAziendaTipoProcedimento) {
        this.idAziendaTipoProcedimento = idAziendaTipoProcedimento;
    }

    public Map<Integer, Operations> getNodeInvolved() {
        return nodeInvolved;
    }

    public void setNodeInvolved(Map<Integer, Operations> nodeInvolved) {
        this.nodeInvolved = nodeInvolved;
    }

}
