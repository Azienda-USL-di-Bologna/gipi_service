/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import java.util.Date;

/**
 *
 * @author utente
 */
public class SospensioneParams {
    public int idIter;
    public int idUtente;
    public String codiceRegistroDocumento;
    public String numeroDocumento;
    public int annoDocumento;
    public Date sospesoDal;
    public Date sospesoAl;
    public String note;

    public int getIdIter() {
        return idIter;
    }

    public void setIdIter(int idIter) {
        this.idIter = idIter;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }


    public String getCodiceRegistroDocumento() {
        return codiceRegistroDocumento;
    }

    public void setCodiceRegistroDocumento(String codiceRegistroDocumento) {
        this.codiceRegistroDocumento = codiceRegistroDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public int getAnnoDocumento() {
        return annoDocumento;
    }

    public void setAnnoDocumento(int annoDocumento) {
        this.annoDocumento = annoDocumento;
    }

    public Date getSospesoDal() {
        return sospesoDal;
    }

    public void setSospesoDal(Date sospesoDal) {
        this.sospesoDal = sospesoDal;
    }

    public Date getSospesoAl() {
        return sospesoAl;
    }

    public void setSospesoAl(Date sospesoAl) {
        this.sospesoAl = sospesoAl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


  
}
