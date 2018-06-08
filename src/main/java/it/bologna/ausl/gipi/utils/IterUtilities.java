/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.gipi.utils.classes.GestioneStatiParams;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import okhttp3.MultipartBody;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */

@Component
public class IterUtilities {
    @Autowired
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Value("${updateGdDoc}")
    private String updateGdDocPath;
    
    /* Fascicolo il documento */
    public Response inserisciFascicolazione(Iter i, GestioneStatiParams gestioneStatiParams, String cfUtenteFascicolatore) throws IOException{
        
        String baseUrl = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);

        // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        String urlChiamata = baseUrl + updateGdDocPath;
        
        GdDoc g = new GdDoc((String) gestioneStatiParams.getIdOggettoOrigine(), gestioneStatiParams.getTipoOggettoOrigine(), null, null, null, null, null, (String) gestioneStatiParams.getCodiceRegistroDocumento(), null, null, null, null, null, null, null, null, null, null);
        Fascicolazione fascicolazione = new Fascicolazione(i.getIdFascicolo(), null, null, null, DateTime.now(), Document.DocumentOperationType.INSERT);
        fascicolazione.setCfUtenteFascicolatore(cfUtenteFascicolatore);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        okhttp3.RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                .build();
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        System.out.println("La richiesta --> " + requestg.toString());
        Response responseg = client.newCall(requestg).execute();
//        if (!responseg.isSuccessful()) {
//            System.out.println("La risposta --> " + responseg.toString());
//            throw new IOException("La fascicolazione non è andata a buon fine.");
//        } 
        
        return responseg;
    }    
    
}
