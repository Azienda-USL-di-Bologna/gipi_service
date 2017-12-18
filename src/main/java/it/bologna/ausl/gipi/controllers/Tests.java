/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import it.bologna.ausl.gipi.process.Process;
import java.text.ParseException;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author user
 */
@RestController
@RequestMapping("/gipi/resources/custom/tests")
public class Tests {

    @Autowired
    Process process;

//    @RequestMapping(value = "getNextFase", method = RequestMethod.GET)
//    public ResponseEntity getNextFase() {
//
//        Fase fase = new Fase();
//        fase.setId(1);
//        fase.setNomeFase("Prova");
//        fase.setOrdinale(2);
//
//        process.get(fase);
//        return new ResponseEntity(HttpStatus.OK);
//    }
    @RequestMapping(value = "getNextFase", method = RequestMethod.GET)
    public ResponseEntity getNextFase() {
        Iter iter = new Iter();
        iter.setId(6);
        Fase fase = new Fase();
        fase.setId(2);
        iter.setIdFase(fase);
        process.getNextFase(iter);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "stepOn", method = RequestMethod.GET)
    public ResponseEntity stepOn() throws ParseException {

        Iter iter = new Iter();
        iter.setId(6);
        Fase fase = new Fase();
        fase.setId(2);
        iter.setIdFase(fase);

        process.stepOn(iter, null);
        return new ResponseEntity(HttpStatus.OK);
    }

}
