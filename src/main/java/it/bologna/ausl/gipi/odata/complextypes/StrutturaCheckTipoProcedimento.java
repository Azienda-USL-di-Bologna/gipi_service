/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.odata.complextypes;

import it.bologna.ausl.entities.baborg.Struttura;
import it.nextsw.olingo.edmextension.annotation.EdmSimpleProperty;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
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
public class StrutturaCheckTipoProcedimento extends Struttura {

    public StrutturaCheckTipoProcedimento() {
        super();
        this.checked = false;
    }

    public StrutturaCheckTipoProcedimento(Struttura s) {
        super(s.getId(), s.getCodice(), s.getNome(), s.getAttiva(), s.getSpettrale(), s.getUsaSegreteriaBucataPadre());
        this.id = s.getId();
        this.checked = false;
    }

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Boolean)
    private boolean checked;

    @EdmSimpleProperty(type = EdmSimpleTypeKind.Int32)
    private Integer id;

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
