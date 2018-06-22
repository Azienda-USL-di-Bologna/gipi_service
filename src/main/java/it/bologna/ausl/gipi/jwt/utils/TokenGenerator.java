/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.jwt.utils;

import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.generator.JWTGenerator;
import it.bologna.ausl.gipi.exceptions.GipiPubblicazioneException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */

@Component
public class TokenGenerator {
    
    @Value("${gipi.keystore.password}")
    private String keyStorePassword;
    
    @Value("${gipi.keystore.alias-ambiente}")
    private String keyStoreEntry;
    
    @Value("${gipi.keystore.authority}")
    private String certAuthority;
    
    @Value("${mode}")
    private String mode;
    
    private static final Logger log = LoggerFactory.getLogger(TokenGenerator.class);
    
    public String getToken(Azienda azienda) throws GipiPubblicazioneException, IOException {
        String token;
        try {
            JWTGenerator jWTGenerator = new JWTGenerator();
            Key key = jWTGenerator.generateKey(JWTGenerator.AMBIENTE.valueOf(keyStoreEntry), keyStorePassword);
            String codiceAziendaConRegione = azienda.getCodiceRegione() + azienda.getCodice();
            token = jWTGenerator.createJWS(key, certAuthority, "gipi", mode, codiceAziendaConRegione);
        }
        catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | JoseException ex) {
            throw new GipiPubblicazioneException("errore nella generazione del token", ex);
        }
        log.info("token generato: " + token);
        return token;
    }
}
