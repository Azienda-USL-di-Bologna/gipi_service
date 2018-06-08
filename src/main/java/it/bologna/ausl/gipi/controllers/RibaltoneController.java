/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.cache.cachableobject.RuoloCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.repository.RibaltoniDaLanciareRepository;
import it.bologna.ausl.entities.utilities.response.controller.ControllerHandledExceptions;
import it.bologna.ausl.entities.utilities.response.exceptions.BadRequestResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.ConflictResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.ForbiddenResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.InternalServerErrorResponseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author f.gusella
 */
@RestController
@RequestMapping(value = "${custom.mapping.url.root}" + "/ribaltone")
public class RibaltoneController extends ControllerHandledExceptions {
    
    @Autowired
    private RibaltoniDaLanciareRepository ribaltoniDaLanciareRepository;
    
    private final String ribaltoniDaLanciareUniqueIndex = "ribaltoni_da_lanciare_codice_azienda_email_idx";
        
    /**
     *
     * @param params
     * @return 
     */
    @RequestMapping(value = "lanciaRibaltone", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity ribaltaAzienda(@RequestBody RibaltoneParams params) {
        
        // Controllo di avere i parametri necessari. Se non li ho torno bad request
        if(!StringUtils.hasText(params.getCodiceAzienda()) || !StringUtils.hasText(params.getIndirizzoMail())) {
            throw new BadRequestResponseException(0, "Attenzione, i parametri per il lancio del ribaltone non sono corretti.", "");
        }
        
        // Mi prendo l'utente cacheable per sapere se l'utente ha il ruolo di demiurgo.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        List<RuoloCachable> ruoliCachable = userInfo.getRuoliUtente();
        
        // Controllo se l'utente ha il ruolo di super demiurgo. Altrimenti torno Forbidden
        if (ruoliCachable.stream().anyMatch(ruolo -> ruolo.getNomeBreve() == Ruolo.CodiciRuolo.SD)) {
            try {
                ribaltoniDaLanciareRepository.inserisciRibaltoneDaLanciare(params.getCodiceAzienda(), params.getIndirizzoMail(), (Integer) userInfo.get(UtenteCachable.KEYS.ID));
            } catch(JpaSystemException ex) {
                if (ex.getRootCause() != null && ex.getRootCause().getMessage().contains(ribaltoniDaLanciareUniqueIndex)) {
                    throw new ConflictResponseException(0, "Attenzione, è già previsto il lancio del ribaltone su questa azienda e per questa mail.\n Se la mail non dovesse arrivare entro qualche minuto si prega di contattare Babelcare.", ex.getMessage());
                }
                throw new InternalServerErrorResponseException(0,"Attenzione, errore legato al database non previsto. Contattare Babelcare.", ex.getMessage());
            } catch(Exception ex) {
                throw new InternalServerErrorResponseException(0,"Attenzione, errore generico non previsto. Contattare Babelcare.", ex.getMessage());
            }
        } else {
            throw new ForbiddenResponseException(0, "Attenzione, non sei abilitato all'utilizzo di questa funzione.", "");
        }
        
        return new ResponseEntity("ok", HttpStatus.OK);
    }    

    public static class RibaltoneParams {
        private String codiceAzienda;
        private String indirizzoMail;
        
        public RibaltoneParams() {
        }

        public String getCodiceAzienda() {
            return codiceAzienda;
        }

        public void setCodiceAzienda(String codiceAzienda) {
            this.codiceAzienda = codiceAzienda;
        }

        public String getIndirizzoMail() {
            return indirizzoMail;
        }

        public void setIndirizzoMail(String indirizzoMail) {
            this.indirizzoMail = indirizzoMail;
        }
    }
}
