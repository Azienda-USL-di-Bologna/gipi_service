package it.bologna.ausl.gipi.config.scheduler;

import java.util.Objects;

/**
 * classe di rappresentazione della chiave da usare nella ConcurrenthashMap, che
 * identifica il nome del servizio e l'azienda, se esiste, associata
 *
 * @author spritz
 */
public class ServiceKey {

    private String serviceName;
    private Integer idAzienda;

    public ServiceKey(String serviceName, Integer idAzienda) {
        this.serviceName = serviceName;
        this.idAzienda = idAzienda;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getIdAzienda() {
        return idAzienda;
    }

    public void setIdAzienda(Integer idAzienda) {
        this.idAzienda = idAzienda;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ServiceKey)) {
            return false;
        }
        ServiceKey other = (ServiceKey) object;
        if ((this.serviceName == null && other.serviceName != null) || (this.serviceName != null && !this.serviceName.equals(other.serviceName))
                || (this.idAzienda == null && other.idAzienda != null) || (this.idAzienda != null && !this.idAzienda.equals(other.idAzienda))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.serviceName);
        hash = 67 * hash + Objects.hashCode(this.idAzienda);
        return hash;
    }
}
