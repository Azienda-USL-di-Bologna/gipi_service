/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.config.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.AziendaParametriJson;
import it.bologna.ausl.entities.repository.AziendaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author f.gusella
 */
@Configuration
public class RedisConfiguration {

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AziendaRepository aziendaRepository;

    @Bean(name = {"BabelSuiteJedisPoolMap"})
    public Map<String, JedisPool> babelSuiteJedisPoolMap() {
        List<Azienda> aziende = aziendaRepository.findAll();
        Map<String, JedisPool> res = new HashMap<>();

        aziende.stream().forEach(a -> {
            try {
                AziendaParametriJson aziendaParametriJson = AziendaParametriJson.parse(objectMapper, a.getParametri());
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(128);
                poolConfig.setBlockWhenExhausted(false);
                JedisPool jp = new JedisPool(poolConfig, aziendaParametriJson.getMasterchefParams().getRedisHost(), aziendaParametriJson.getMasterchefParams().getRedisPort());
                res.put(a.getCodice(), jp);
            } catch (Exception e) {
                // TODO: usare il log slf4j
                e.printStackTrace();
            }
        });

        return res;
    }
}
