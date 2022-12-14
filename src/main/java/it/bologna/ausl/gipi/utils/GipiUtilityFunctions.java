/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.AziendaParametriJson;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.repository.UtenteRepository;
import it.bologna.ausl.masterchefclient.JobParams;
import it.bologna.ausl.masterchefclient.PrimusCommanderParams;
import it.bologna.ausl.masterchefclient.WorkerData;
import it.bologna.ausl.primuscommanderclient.PrimusCommand;
import it.bologna.ausl.primuscommanderclient.PrimusCommandParams;
import it.bologna.ausl.primuscommanderclient.PrimusMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 * @author f.gusella
 */
@Component
@Qualifier("GipiUtilityFunctions")
public class GipiUtilityFunctions {
    
    @Autowired
    @Qualifier ("BabelSuiteJedisPoolMap")
    Map<String, JedisPool> babelSuiteJedisPoolMap;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    UtenteRepository utenteRepository;
    
    /**
     * 
     * @param azienda
     * @param command
     * @param utenti lista dei codici fiscali degli utenti che riceveranno il comando 
     * @throws java.io.UnsupportedEncodingException 
     */
    public void sendPrimusCommand(Azienda azienda, List<String> utenti, PrimusCommandParams command, String idApplicazione) throws UnsupportedEncodingException, IOException {
        AziendaParametriJson aziendaParametriJson = AziendaParametriJson.parse(objectMapper, azienda.getParametri());
        AziendaParametriJson.MasterChefParmas masterChefParmas = aziendaParametriJson.getMasterchefParams();
        String masteChefInQueue = masterChefParmas.getInQueue();
        
        if (idApplicazione == null || idApplicazione.equals(""))
            idApplicazione = "*";
        
        PrimusCommand com = new PrimusCommand(command);
        PrimusMessage m = new PrimusMessage(utenti, idApplicazione, com);

        JobParams j = new PrimusCommanderParams("1", "1", m);
        String tempQueue = "gipi_service_" + UUID.randomUUID();
        WorkerData w = new WorkerData("gipi", "1", tempQueue, 5);
        w.addNewJob("1", "", j);
  
        JedisPool jedisPool = babelSuiteJedisPoolMap.get(azienda.getCodice());
        try (Jedis jd = jedisPool.getResource()) {
            jd.rpush(masteChefInQueue, w.getStringForRedis());
        }
    }
    
    public Utente getUtenteLoggatto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        return utenteRepository.findById((Integer) userInfo.get(UtenteCachable.KEYS.ID)).get();
    }
}
