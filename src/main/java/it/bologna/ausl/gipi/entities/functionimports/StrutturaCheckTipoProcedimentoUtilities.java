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
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Andrea Pasquini
 */
@EdmFunctionImportClass
@Component
public class StrutturaCheckTipoProcedimentoUtilities {

    //@Value("${nomeparamaetro})
    //private string querytext;
    @Value("${functionimports.query-strutture-con-check}")
    private String queryStruttureText;

    private static final Logger logger = Logger.getLogger(StrutturaCheckTipoProcedimentoUtilities.class);

    @Autowired
    private EntityManager em;

    @EdmFunctionImport(
            name = "GetStruttureByTipoProcedimento",
            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.COMPLEX, formatResult = EdmFunctionImport.FormatResult.COLLECTION),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )

    public List<StrutturaCheckTipoProcedimento> GetStruttureByTipoProcedimento(
            @EdmFunctionImportParameter(name = "idTipoProcedimento", facets = @EdmFacets(nullable = false))
            final Integer idTipoProcedimento,
            @EdmFunctionImportParameter(name = "idAzienda", facets = @EdmFacets(nullable = false))
            final Integer idAzienda
    ) {

        //Test q = (Test) em.createNativeQuery("SELECT * FROM organigramma.strutture limit 1", "Test").getSingleResult();
        // Test s = (Test) em.createNativeQuery("SELECT * FROM organigramma.strutture limit 1").getSingleResult();
        //q.setParameter(1, 1);
//        StrutturaCheckTipoProcedimento s = (StrutturaCheckTipoProcedimento) q.getSingleResult();
        //Struttura s = (Struttura) q.getSingleResult();
//        StrutturaCheckTipoProcedimento ssss = new StrutturaCheckTipoProcedimento();
        Query q = em.createNativeQuery(queryStruttureText);
        q.setParameter(1, idTipoProcedimento);
        q.setParameter(2, idTipoProcedimento);
        q.setParameter(3, idAzienda);
        

        List<Object[]> arrayObj = q.getResultList();

        List<StrutturaCheckTipoProcedimento> arrayStruttureConCheck = new ArrayList<StrutturaCheckTipoProcedimento>();

        for (Object[] o : arrayObj) {

            StrutturaCheckTipoProcedimento strutturaConCheck = new StrutturaCheckTipoProcedimento();

            //id [0]
            //id_azienda [1]
            //id_struttura_padre [2]
            //nome [3]
            //checked [4]
            
            strutturaConCheck.setId((Integer) o[0]);
            strutturaConCheck.setIdStrutturaPadre((Integer) o[2]);
            strutturaConCheck.setNome(o[3].toString());
            strutturaConCheck.setChecked((boolean) o[4]);

            arrayStruttureConCheck.add(strutturaConCheck);
        }

        return arrayStruttureConCheck;
    }

}
