package it.bologna.ausl.gipi.pubblicazioni;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mappa la classe Azienda presente su SHALBO
 * @author Giuseppe Russo <g.russo@nsi.it>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Azienda implements Marshaller{

    private Integer id;
    private String path;
    private String parametri;
    private String codiceRegioneAzienda;

    public Azienda() {
    }

    public Azienda(Integer id) {
        this.id = id;
    }

    public Azienda(Integer id, String path) {
        this.id = id;
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParametri() {
        return parametri;
    }

    public void setParametri(String parametri) {
        this.parametri = parametri;
    }

    public String getCodiceRegioneAzienda() {
        return codiceRegioneAzienda;
    }

    public void setCodiceRegioneAzienda(String codiceRegioneAzienda) {
        this.codiceRegioneAzienda = codiceRegioneAzienda;
    }
        
    @Override
    public String toString() {
        return "it.bologna.ausl.gipi.pubblicazioni.Azienda[ id=" + id + " ]";
    }
}
