package it.bologna.ausl.gipi.entities.functionimports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QDocumentoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.utilities.FunctionImportSorting;
import it.bologna.ausl.gipi.controllers.IterController;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrls;
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

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 *
 * @author f.gusella
 */
@EdmFunctionImportClass
@Component
public class GetIterUtente extends EdmFunctionImportClassBase implements FunctionImportSorting {

    private static final Logger log = LoggerFactory.getLogger(GetIterUtente.class);

    public static enum GetFascicoliUtente {
        TIPO_FASCICOLO, SOLO_ITER, CODICE_FISCALE
    }

    @Value("${getFascicoliUtente}")
    private String baseUrlBdsGetFascicoliUtente;

    @Autowired
    private Sql2o sql2o;
    
    @Autowired
    ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager em;
    
    @Value("${functionimports.query-iter-stato-non-cambiabile}")
    private String incredibleQuery;

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
            @EdmFunctionImportParameter(name = "annoDocumento", facets = @EdmFacets(nullable = true)) final Integer annoDocumento,
            @EdmFunctionImportParameter(name = "idOggettoOrigine", facets = @EdmFacets(nullable = true)) final String idOggettoOrigine,
            // filtri
            @EdmFunctionImportParameter(name = "oggetto", facets = @EdmFacets(nullable = true)) final String oggetto,
            @EdmFunctionImportParameter(name = "numero", facets = @EdmFacets(nullable = true)) final Integer numero,
            @EdmFunctionImportParameter(name = "dataAvvio", facets = @EdmFacets(nullable = true)) final String dataAvvio,
            @EdmFunctionImportParameter(name = "idStato_sep_descrizione", facets = @EdmFacets(nullable = true)) final String descrizioneStato,
            @EdmFunctionImportParameter(name = "idResponsabileProcedimento_sep_idPersona_sep_descrizione", facets = @EdmFacets(nullable = true)) final String descrizioneRespProc,
            @EdmFunctionImportParameter(name = "dataRegistrazione", facets = @EdmFacets(nullable = true)) final String dataRegistrazione,
            // sort
            @EdmFunctionImportParameter(name = "sort", facets = @EdmFacets(nullable = true)) final String sort
    ) throws IOException {
        log.info("sono in getIterUtente, idAzienda: " + idAzienda + ", cf: " + cf);
        log.info("il documento, se passato, e': " + codiceRegistro + ", " + numeroDocumento + ", " + annoDocumento + ", " + idOggettoOrigine);

        log.info("sort: " + sort);
        
        Researcher r = new Researcher(null, null, 0);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(IterController.GetFascicoli.TIPO_FASCICOLO.toString(), "2");
        additionalData.put(IterController.GetFascicoli.SOLO_ITER.toString(), "true");
        additionalData.put(IterController.GetFascicoli.CODICE_FISCALE.toString(), cf);
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", r, additionalData);

        String baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(idAzienda, em, objectMapper) + baseUrlBdsGetFascicoliUtente;
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

        /* QUESTA NON E' PIU' UTILIZZATA */
        if (StringUtils.hasText(dataRegistrazione)) {
            // raffinare la lista iter
            log.info("***LISTA --> " + listaIter.toString());
            log.info("***TOTALE LISTA -->" + listaIter.size());
            List<Integer> iterDaEscludere = new ArrayList<>();
            
            try (Connection con = sql2o.open()) {
                iterDaEscludere = con.createQuery(incredibleQuery)
                        .addParameter("data_ora_evento", dataRegistrazione)
                        .addParameter("lista_iter", listaIter)
                        //.withParams(paramValues)addParameter("lista_iter", java.util.List.class<Integer>, listaIter)
                        .executeAndFetch(Integer.class);
            }
            log.info("***LISTA da escludere --> " + iterDaEscludere.toString());
            log.info("***TOTALE LISTA da esclidere-->" + iterDaEscludere.size());
//            iterDaEscludere.sort(null);
            listaIter.removeAll(iterDaEscludere);
            log.info("***LISTA raffinata --> " + listaIter.toString());
            log.info("***TOTALE LISTA raffinata -->" + listaIter.size());
        }
        
        if (idOggettoOrigine != null && !idOggettoOrigine.equals("")) {
            queryDSL
                    .select(QIter.iter)
                    .from(QIter.iter)
                    .leftJoin(QIter.iter.documentiIterList, QDocumentoIter.documentoIter)
                    .on(QDocumentoIter.documentoIter.idOggetto.eq(idOggettoOrigine))
                    //                    .on(QDocumentoIter.documentoIter.numeroRegistro.eq(numeroDocumento)
                    //                            .and(QDocumentoIter.documentoIter.registro.eq(codiceRegistro)
                    //                                    .and(QDocumentoIter.documentoIter.anno.eq(annoDocumento))))
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

        if (StringUtils.hasText(oggetto)) {
            queryDSL.where(QIter.iter.oggetto.likeIgnoreCase("%" + oggetto + "%"));
        }
        if (numero != null) {
            queryDSL.where(QIter.iter.numero.stringValue().like("%" + Integer.toString(numero) + "%"));
        }
        if (StringUtils.hasText(descrizioneStato)) {
            queryDSL.where(QIter.iter.idStato.descrizione.likeIgnoreCase("%" + descrizioneStato + "%"));
        }
        if (StringUtils.hasText(descrizioneRespProc)) {
            queryDSL.where(QIter.iter.idResponsabileProcedimento.idPersona.descrizione.likeIgnoreCase("%" + descrizioneRespProc + "%"));
        }

        
        if (sort != null && !sort.isEmpty()) {
            addSorting(queryDSL, sort, Iter.class);
        } else {
            queryDSL.orderBy(QIter.iter.numero.desc());
        }
        return createQueryInfo(queryDSL, QIter.iter.id.count(), em);
    }
}
