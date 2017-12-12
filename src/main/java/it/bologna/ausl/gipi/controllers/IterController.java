/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.entities.gipi.Iter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author f.gusella
 */
@RestController
@RequestMapping("/gipi/resources/custom/")
@PropertySource("classpath:query.properties")
public class IterController {
    
    @RequestMapping(value = "avviaNuovoIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity AvviaNuovoIter(@RequestBody IterParams data) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        Class<?> clazz = data.getClass();
//        Field field = clazz.getField("iter"); //Note, this can throw an exception if the field doesn't exist.
//        Object fieldValue = field.get(data);

        System.out.println("QWWEEEEEEEEEE");
        System.out.println(data.getDataAvvio());
        System.out.println(data.getDataCreazione());
        System.out.println(data.getOggetto());
        System.out.println(data.getFK_id_responsabile_procedimento());
        System.out.println(data.getId());      
        // Devo salvare l'iter, il procedimento_cache, la fase iter, l'evento iter, creare il fascicolo dell'iter
//        return new ResponseEntity(new ArrayList<Object>() , HttpStatus.OK);
        return new ResponseEntity(data , HttpStatus.OK);
    }
}
