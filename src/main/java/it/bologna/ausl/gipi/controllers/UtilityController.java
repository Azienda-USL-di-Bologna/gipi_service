package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author andrea
 */
@RestController
@RequestMapping(value = "${utility.mapping.url.root}")
public class UtilityController {

    @Value("${revision}")
    private String revision;
    @Value("${modificationTime}")
    private String modificationTime;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "version", method = RequestMethod.GET)
    public ResponseEntity<String> getVersion() {
        return new ResponseEntity<>(revision + "\n" + modificationTime, HttpStatus.OK);
    }

    @Cacheable("prima")
    @RequestMapping(value = "caca", method = RequestMethod.GET)
    public String getCaca() {
        return revision + "\n" + modificationTime;
        //return new ResponseEntity<>(revision + "\n" + modificationTime, HttpStatus.OK);
    }

    @RequestMapping(value = "save", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
    public void save(@RequestBody AziendaTipoProcedimento entity) {

//        AziendaTipoProcedimento aziendaTipoProcedimento = objectMapper.convertValue(entity, AziendaTipoProcedimento.class);
        System.out.println("aziendaTipoProcedimento: " + entity);
    }

}
