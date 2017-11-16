/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.odata.complextypes;

import it.bologna.ausl.entities.baborg.Struttura;
import it.nextsw.olingo.edmextension.annotation.EdmSimpleProperty;
import java.util.Date;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.SqlResultSetMapping;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.springframework.stereotype.Component;

/**
 * questa classe è un estensione della classe struttura e viene utilizzata per
 * popolare l'albero delle strutture nel quale si vorranno vedere i check sulle
 * strutture nel caso queste siano associate ad un'altra entità. ES: il caso in
 * cui apro la videata di gipi che permette di modificare un tipo di
 * procedimento associato ad una struttura: quanto apro l'interfaccia, l'albero
 * deve essere popolato con tutte le strutture e, su tutte quelle che hanno
 * associato lo stesso tipo di procedimento (che stiamo sotto analizzando)
 * devono mostrare il check
 *
 * @author Andrea Pasquini
 */
@EdmComplexType
@Component
public class StrutturaCheckTipoProcedimento {

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer id;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.String)
    private String nome;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer idAzienda;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer idStrutturaPadre;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private Boolean checked;

    public StrutturaCheckTipoProcedimento() {
    }

    public StrutturaCheckTipoProcedimento(Integer id, String nome, Integer idAzienda, Integer idStrutturaPadre, Boolean checked) {
        this.id = id;
        this.nome = nome;
        this.idAzienda = idAzienda;
        this.idStrutturaPadre = idStrutturaPadre;
        this.checked = checked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getIdStrutturaPadre() {
        return idStrutturaPadre;
    }

    public void setIdStrutturaPadre(Integer idStrutturaPadre) {
        this.idStrutturaPadre = idStrutturaPadre;
    }

    public Integer getIdAzienda() {
        return idAzienda;
    }

    public void setIdAzienda(Integer idAzienda) {
        this.idAzienda = idAzienda;
    }

    public Boolean isChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

}
