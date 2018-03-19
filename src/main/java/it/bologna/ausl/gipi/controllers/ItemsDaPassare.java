/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 *
 * @author g.russo
 */
public class ItemsDaPassare {
        public Integer idIter;
        public String idResponsabileProcedimento;
        public String numero;
        public Integer anno;
        public String tipoOggettoOrigine;

    public ItemsDaPassare() {
    }

        public ItemsDaPassare(Integer idIter, String idResponsabileProcedimento, String numero, Integer anno, String tipoOggettoOrigine) {
            this.idIter = idIter;
            this.idResponsabileProcedimento = idResponsabileProcedimento;
            this.numero = numero;
            this.anno = anno;
            this.tipoOggettoOrigine = tipoOggettoOrigine;
        }
               
        public Integer getIdIter() {
            return idIter;
        }

        public void setIdIter(Integer idIter) {
            this.idIter = idIter;
        }

        public String getIdResponsabileProcedimento() {
            return idResponsabileProcedimento;
        }

        public void setIdResponsabileProcedimento(String idResponsabileProcedimento) {
            this.idResponsabileProcedimento = idResponsabileProcedimento;
        }

        public String getNumero() {
            return numero;
        }

        public void setNumero(String numero) {
            this.numero = numero;
        }

        public Integer getAnno() {
            return anno;
        }

        public void setAnno(Integer anno) {
            this.anno = anno;
        }

        public String getTipoOggettoOrigine() {
            return tipoOggettoOrigine;
        }

        public void setTipoOggettoOrigine(String tipoOggettoOrigine) {
            this.tipoOggettoOrigine = tipoOggettoOrigine;
        }
          
        @JsonIgnore
    public String getJSONString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        String writeValueAsString = mapper.writeValueAsString(this);
        return writeValueAsString;
    }
        
    }