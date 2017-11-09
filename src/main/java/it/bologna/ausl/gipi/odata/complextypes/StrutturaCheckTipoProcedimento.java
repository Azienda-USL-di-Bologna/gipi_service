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
//@SqlResultSetMapping(
//        name = "StrutturaCheckTipoProcedimento",
//        classes = {
//            @ConstructorResult(
//                    targetClass = StrutturaCheckTipoProcedimento.class,
//                    columns = {
//                        @ColumnResult(name = "id")
//                        ,
//                    @ColumnResult(name = "name")
//                        ,
//                    @ColumnResult(name = "orderCount")
//                        ,
//                    @ColumnResult(name = "avgOrder", type = Double.class)
//                    }
//            )
//        }
//)
public class StrutturaCheckTipoProcedimento {

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer id;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private int codice;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.String)
    private String nome;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.String)
    private String codiceDislocazione;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.String)
    private String dislocazione;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.DateTime)
    private Date dataAttivazione;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.DateTime)
    private Date dataCessazione;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private boolean attiva;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private boolean spettrale;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private boolean usaSegreteriaBucataPadre;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer idStrutturaPadre;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private boolean checked;

    public StrutturaCheckTipoProcedimento() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodiceDislocazione() {
        return codiceDislocazione;
    }

    public void setCodiceDislocazione(String codiceDislocazione) {
        this.codiceDislocazione = codiceDislocazione;
    }

    public String getDislocazione() {
        return dislocazione;
    }

    public void setDislocazione(String dislocazione) {
        this.dislocazione = dislocazione;
    }

    public Date getDataAttivazione() {
        return dataAttivazione;
    }

    public void setDataAttivazione(Date dataAttivazione) {
        this.dataAttivazione = dataAttivazione;
    }

    public Date getDataCessazione() {
        return dataCessazione;
    }

    public void setDataCessazione(Date dataCessazione) {
        this.dataCessazione = dataCessazione;
    }

    public boolean isAttiva() {
        return attiva;
    }

    public void setAttiva(boolean attiva) {
        this.attiva = attiva;
    }

    public boolean isSpettrale() {
        return spettrale;
    }

    public void setSpettrale(boolean spettrale) {
        this.spettrale = spettrale;
    }

    public boolean isUsaSegreteriaBucataPadre() {
        return usaSegreteriaBucataPadre;
    }

    public void setUsaSegreteriaBucataPadre(boolean usaSegreteriaBucataPadre) {
        this.usaSegreteriaBucataPadre = usaSegreteriaBucataPadre;
    }

    public Integer getIdStrutturaPadre() {
        return idStrutturaPadre;
    }

    public void setIdStrutturaPadre(Integer idStrutturaPadre) {
        this.idStrutturaPadre = idStrutturaPadre;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
