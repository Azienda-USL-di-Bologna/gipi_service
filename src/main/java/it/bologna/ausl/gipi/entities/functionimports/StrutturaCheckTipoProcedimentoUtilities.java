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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author Andrea Pasquini
 */
@EdmFunctionImportClass
@Component
public class StrutturaCheckTipoProcedimentoUtilities {

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
            final Integer idAzienda) {

        List<Object[]> rawResultList;
        List<StrutturaCheckTipoProcedimento> result = new ArrayList<>();

        Query query = em.createNativeQuery(queryStruttureText);
        query.setParameter(1, idTipoProcedimento);
        query.setParameter(2, idTipoProcedimento);
        query.setParameter(3, idAzienda);

        rawResultList = query.getResultList();

        try {
            List<Object> lista = getStruttureWithCheck(StrutturaCheckTipoProcedimento.class, rawResultList);

            for (Iterator<Object> iterator = lista.iterator(); iterator.hasNext();) {
                result.add((StrutturaCheckTipoProcedimento) iterator.next());
            }

            return result;

        } catch (Exception ex) {
            logger.error("can not create complex type from SQL");
        }

        return null;
    }

    private List<Object> getStruttureWithCheck(Class classz, List<Object[]> listOfRecords) throws NoSuchMethodException, SecurityException, InstantiationException {

        ArrayList<Object> res = new ArrayList<>();
        
        Class[] arrayOfType = new Class[classz.getDeclaredFields().length];

        // array di tipi calcolati sulle proprietà definite nella classe
        for (int i = 0; i < classz.getDeclaredFields().length; i++) {
            arrayOfType[i] = classz.getDeclaredFields()[i].getType();
        }

        for (Object[] record : listOfRecords) {

            Object[] arrayOfValue = new Object[record.length];

            // array di valori
            for (int i = 0; i < arrayOfValue.length; i++) {
                arrayOfValue[i] = record[i];
            }

            Constructor<Object> constr = classz.asSubclass(classz).getConstructor(arrayOfType);
            try {
                res.add(constr.newInstance(arrayOfValue));
            } catch (Exception ex) {
                logger.error("cannot instantiating object");
            }
        }

        return res;
    }

    //        List<StrutturaExt> listStrutturaExt = getResultList(query, StrutturaExt.class);
//        for (Object resultElement : rawResultList) {
//            // Safety check before casting the object
//            if (resultElement instanceof StrutturaExt) {
//                System.out.println("");
//            } else {
//                System.out.println("");
//                // other type of object. Handle it separately
//            }
//        }
//        Query q = em.createNativeQuery(queryStruttureText, StrutturaExt.class);
//        q.setParameter(1, idTipoProcedimento);
//        q.setParameter(2, idTipoProcedimento);
//        q.setParameter(3, idAzienda);
//
//        StrutturaExt s = (StrutturaExt) q.getSingleResult();
}
