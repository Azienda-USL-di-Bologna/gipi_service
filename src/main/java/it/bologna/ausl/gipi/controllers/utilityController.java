/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author andrea
 */
@RestController
@RequestMapping("/gipi/utility")
public class utilityController {

    @Value("${revision}")
    private String revision;
    @Value("${modificationTime}")
    private String modificationTime;

    @RequestMapping(value = "version", method = RequestMethod.GET)
    public ResponseEntity<String> getVersion() {
        return new ResponseEntity<>(revision + "\n" + modificationTime, HttpStatus.OK);
    }

}
