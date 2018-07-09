/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi;

import it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author l.salomone
 */


public class WebApiBabelTest {
    
    private final String GET_ID_UTENTI_MAP_PATH = "/Babel/GetIdUtentiMap";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    public static void testaLaWebApi() throws UnsupportedEncodingException, IOException, ParseException {
        JSONObject o = new JSONObject();
        JSONArray lista = new JSONArray();
        lista.add("SLMLNZ85C13A944M");
        lista.add("MGGCHR76R63A944X");
        lista.add("TSCMNN78H66E435S");
        lista.add("MGLRSY90B53A944S");
        lista.add("xfasdfg");

        o.put("listaCF", lista.toString());
        
        String urlChiamata = "http://localhost:8080/Babel/GetIdUtentiMap";
        
        
        System.out.println(o.toString());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
              
        OkHttpClient client = new OkHttpClient();
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "getIdUtentiMap")
                .post(body)
                .build();

        Response responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a Babel non è andata a buon fine.");
        }
        
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseg.body().string());
        System.out.println(json.toString());
//        JSONArray listaRes = (JSONArray) json.get("risultato");
//        System.out.println(listaRes.toString());
        JSONObject risultato = (JSONObject) parser.parse((String) json.get("risultato"));
        System.out.println(risultato.toString());
        
        for(int i = 0; i < lista.size(); i ++) {
            String idUtente = (String) risultato.get(lista.get(i));
            System.out.println(idUtente);
        }
    }
    
    
    public static void unTest() {
        JSONObject uno = new JSONObject();
        JSONObject due = new JSONObject();
        JSONObject tre = new JSONObject();
        JSONArray lista = new JSONArray();

        uno.put("uno", "primo_nome");
        due.put("due", "secondo nome");
        tre.put("tre", "terzo nome");
        
        JSONObject primo = new JSONObject();
        JSONObject secondo = new JSONObject();
        JSONObject terzo = new JSONObject();
        primo.put("primo", uno);
        secondo.put("primo", uno);
        secondo.put("secondo", due);
        terzo.put("primo", uno);
        terzo.put("secondo", due);
        terzo.put("terzo", tre);

        lista.add(primo);
        lista.add(secondo);
        lista.add(terzo);
              
        lista.forEach(item->System.out.println(item));
        System.out.println("*********    DOPO    *********");
        JSONObject o = (JSONObject) lista.get(1);
        o.put("terzo", tre);
        lista.forEach(item->System.out.println(item));
    }
    
    public static void main(String[] args) throws IOException, ParseException  {
        // testaLaWebApi();
        
        NotifyScadenzaSospensioneTask nsst= new NotifyScadenzaSospensioneTask();
        nsst.notifyMain();
          
        
        //unTest();
        
    }
}

