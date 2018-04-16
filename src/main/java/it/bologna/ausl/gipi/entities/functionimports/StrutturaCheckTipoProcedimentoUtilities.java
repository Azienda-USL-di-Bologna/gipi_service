package it.bologna.ausl.gipi.entities.functionimports;

import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 *
 * @author Andrea Pasquini
 */
@EdmFunctionImportClass
@Component
@PropertySource("classpath:query.properties")
public class StrutturaCheckTipoProcedimentoUtilities {

//    @Value("${functionimports.query-strutture-con-check}")
//    private String queryStruttureText;
    @Value("${functionimports.query-strutture-con-check}")
    private String queryStruttureText;

    private static final Logger logger = Logger.getLogger(StrutturaCheckTipoProcedimentoUtilities.class);

//    @Autowired
//    private EntityManager em;
    @Autowired
    private Sql2o sql2o;

    @EdmFunctionImport(
            name = "GetStruttureByTipoProcedimento",
            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.COMPLEX, formatResult = EdmFunctionImport.FormatResult.COLLECTION),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )

    public List<StrutturaCheckTipoProcedimento> GetStruttureByTipoProcedimento(
            @EdmFunctionImportParameter(name = "idAziendaTipoProcedimento", facets = @EdmFacets(nullable = false))
            final Integer idAziendaTipoProcedimento,
            @EdmFunctionImportParameter(name = "idAzienda", facets = @EdmFacets(nullable = false))
            final Integer idAzienda) {

//        List<Object[]> rawResultList;
//        List<StrutturaCheckTipoProcedimento> result = new ArrayList<>();
        try (Connection con = sql2o.open()) {
            return con.createQuery(queryStruttureText)
                    .addParameter("id_azienda_tipo_procedimento", idAziendaTipoProcedimento)
                    .addParameter("id_azienda", idAzienda)
                    .addColumnMapping("id_azienda", "idAzienda")
                    .addColumnMapping("id_struttura_padre", "idStrutturaPadre")
                    .executeAndFetch(StrutturaCheckTipoProcedimento.class);
        }

//        Query query = em.createNativeQuery(queryStruttureText);
//        query.setParameter(1, idTipoProcedimento);
//        query.setParameter(2, idTipoProcedimento);
//        query.setParameter(3, idAzienda);
//
//        rawResultList = query.getResultList();
//
//        try {
//            List<Object> lista = getStruttureWithCheck(StrutturaCheckTipoProcedimento.class, rawResultList);
//
//            for (Iterator<Object> iterator = lista.iterator(); iterator.hasNext();) {
//                result.add((StrutturaCheckTipoProcedimento) iterator.next());
//            }
//
//            return result;
//
//        } catch (Exception ex) {
//            logger.error("can not create complex type from SQL");
//        }
//
//        return null;
    }

    @EdmFunctionImport(
            name = "doStruttureByTipoProcedimento",
            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.SIMPLE, formatResult = EdmFunctionImport.FormatResult.SINGLE_OBJECT),
            httpMethod = EdmFunctionImport.HttpMethod.POST
    )
    public boolean Persist() {

        return true;
    }

//    private List<Object> getStruttureWithCheck(Class classz, List<Object[]> listOfRecords) throws NoSuchMethodException, SecurityException, InstantiationException {
//
//        ArrayList<Object> res = new ArrayList<>();
//
//        Class[] arrayOfType = new Class[classz.getDeclaredFields().length];
//
//        // array di tipi calcolati sulle proprietà definite nella classe
//        for (int i = 0; i < classz.getDeclaredFields().length; i++) {
//            arrayOfType[i] = classz.getDeclaredFields()[i].getType();
//        }
//
//        for (Object[] record : listOfRecords) {
//
//            Object[] arrayOfValue = new Object[record.length];
//
//            // array di valori
//            for (int i = 0; i < arrayOfValue.length; i++) {
//                arrayOfValue[i] = record[i];
//            }
//
//            Constructor<Object> constr = classz.asSubclass(classz).getConstructor(arrayOfType);
//            try {
//                res.add(constr.newInstance(arrayOfValue));
//            } catch (Exception ex) {
//                logger.error("cannot instantiating object");
//            }
//        }
//
//        return res;
//    }
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
