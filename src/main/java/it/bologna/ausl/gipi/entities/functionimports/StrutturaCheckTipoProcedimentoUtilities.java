/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.entities.functionimports;

import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QStruttura;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import it.bologna.ausl.gipi.odata.complextypes.Test;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import it.nextsw.olingo.querybuilder.JPAQueryInfo;
import java.util.List;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.TypedQuery;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Andrea Pasquini
 */
@EdmFunctionImportClass
@Component
public class StrutturaCheckTipoProcedimentoUtilities {

    private static final Logger logger = Logger.getLogger(StrutturaCheckTipoProcedimentoUtilities.class);

    @Autowired
    private EntityManager em;

    @EdmFunctionImport(
            name = "GetStruttureByTipoProcedimento",
            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.COMPLEX, formatResult = EdmFunctionImport.FormatResult.SINGLE_OBJECT),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )

    public StrutturaCheckTipoProcedimento GetStruttureByTipoProcedimento(
            @EdmFunctionImportParameter(name = "idTipoProcedimento", facets = @EdmFacets(nullable = false))
            final Integer idTipoProcedimento
    ) {

        //Test q = (Test) em.createNativeQuery("SELECT * FROM organigramma.strutture limit 1", "Test").getSingleResult();
        Struttura s = (Struttura) em.createNativeQuery("SELECT * FROM organigramma.strutture limit 1", Struttura.class).getSingleResult();
        // Test s = (Test) em.createNativeQuery("SELECT * FROM organigramma.strutture limit 1").getSingleResult();
        //q.setParameter(1, 1);
//        StrutturaCheckTipoProcedimento s = (StrutturaCheckTipoProcedimento) q.getSingleResult();
        //Struttura s = (Struttura) q.getSingleResult();
//        StrutturaCheckTipoProcedimento ssss = new StrutturaCheckTipoProcedimento();
        StrutturaCheckTipoProcedimento ss = new StrutturaCheckTipoProcedimento(s);
        ss.setChecked(true);

//        ssss.setNome(s.getNome());
//        ssss.setChecked(true);
        return ss;
    }

}
