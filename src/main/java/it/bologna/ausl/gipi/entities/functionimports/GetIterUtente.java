package it.bologna.ausl.gipi.entities.functionimports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.QDocumentoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.gipi.controllers.IterController;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicoli;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import it.bologna.ausl.ioda.iodaobjectlibrary.Researcher;
import it.nextsw.olingo.edmextension.EdmFunctionImportClassBase;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 * @author f.gusella
 */
@EdmFunctionImportClass
@Component
public class GetIterUtente extends EdmFunctionImportClassBase {

    private static final Logger logger = Logger.getLogger(GetIterUtente.class);

    public static enum GetFascicoliUtente {
        TIPO_FASCICOLO, SOLO_ITER, CODICE_FISCALE
    }

    @Value("${getFascicoliUtente}")
    private String baseUrlBdsGetFascicoliUtente;

    @Autowired
    ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager em;

    @EdmFunctionImport(
            name = "GetIterUtente",
            entitySet = "Iters",
            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.ENTITY, formatResult = EdmFunctionImport.FormatResult.PAGINATED_COLLECTION, EdmEntityTypeName = "Iter"),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )
    public JPAQueryInfo getIterUtente(
            @EdmFunctionImportParameter(name = "cf", facets = @EdmFacets(nullable = false)) final String cf,
            @EdmFunctionImportParameter(name = "idAzienda", facets = @EdmFacets(nullable = false)) final Integer idAzienda,
            @EdmFunctionImportParameter(name = "stato", facets = @EdmFacets(nullable = true)) final String stato,
            @EdmFunctionImportParameter(name = "codiceRegistro", facets = @EdmFacets(nullable = true)) final String codiceRegistro,
            @EdmFunctionImportParameter(name = "numeroDocumento", facets = @EdmFacets(nullable = true)) final String numeroDocumento,
            @EdmFunctionImportParameter(name = "annoDocumento", facets = @EdmFacets(nullable = true)) final Integer annoDocumento
    ) throws IOException {
        logger.info("sono in getIterUtente, idAzienda: " + idAzienda + ", cf: " + cf);
        logger.info("il documento, se passato, e': " + codiceRegistro + ", " + numeroDocumento + ", " + annoDocumento);

        Researcher r = new Researcher(null, null, 0);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(IterController.GetFascicoliUtente.TIPO_FASCICOLO.toString(), "2");
        additionalData.put(IterController.GetFascicoliUtente.SOLO_ITER.toString(), "true");
        additionalData.put(IterController.GetFascicoliUtente.CODICE_FISCALE.toString(), cf);
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", r, additionalData);

        String baseUrl = GetBaseUrl.getBaseUrl(idAzienda, em, objectMapper) + baseUrlBdsGetFascicoliUtente;
        // String baseUrl = "http://localhost:8084/bds_tools/ioda/api/fascicolo/getFascicoliUtente";
        OkHttpClient client = new OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String resString = response.body().string();
        Fascicoli f = (Fascicoli) it.bologna.ausl.ioda.iodaobjectlibrary.Requestable.parse(resString, Fascicoli.class);

        List<Integer> listaIter = new ArrayList<>();

        for (int i = 0; i < f.getSize(); i++) {
            listaIter.add(f.getFascicolo(i).getIdIter());
        }

        JPAQuery queryDSL = new JPAQuery(em);

        if (codiceRegistro != null && !codiceRegistro.equals("")
                && numeroDocumento != null && !numeroDocumento.equals("")
                && annoDocumento != null) {
            queryDSL
                    .select(QIter.iter)
                    .from(QIter.iter)
                    .leftJoin(QIter.iter.documentiIterList, QDocumentoIter.documentoIter)
                    .on(QDocumentoIter.documentoIter.numeroRegistro.eq(numeroDocumento)
                            .and(QDocumentoIter.documentoIter.registro.eq(codiceRegistro)
                                    .and(QDocumentoIter.documentoIter.anno.eq(annoDocumento))))
                    .where(QIter.iter.id.in(listaIter)
                            .and(QDocumentoIter.documentoIter.id.isNull()))
                    .distinct();
        } else {
            queryDSL.select(QIter.iter)
                    .from(QIter.iter)
                    .where(QIter.iter.id.in(listaIter));
        }

        if (StringUtils.hasText(stato)) {
            String[] listaStati = stato.split(":");
            queryDSL.where(QIter.iter.idStato.codice.in(listaStati));
        }

        // queryDSL.orderBy(QIter.iter.idStato.codice.desc());
        return createQueryInfo(queryDSL, QIter.iter.id.count(), em);
    }
}
