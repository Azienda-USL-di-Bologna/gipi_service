package it.bologna.ausl.gipi.config;

import it.bologna.ausl.gipi.odata.processor.JPAServiceFactory;
import java.util.Arrays;
import org.apache.catalina.connector.Connector;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.olingo.odata2.core.rest.ODataExceptionMapperImpl;
import org.apache.olingo.odata2.core.rest.app.ODataApplication;
import org.apache.olingo.odata2.spring.OlingoRootLocator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 *
 * @author gdm
 */
@Configuration
public class ConnectorConfig {

    @Bean
    public EmbeddedServletContainerFactory servletContainer(@Value("${gipi.server.protocol.ajp.port:8202}") int ajpPort) {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();

        /**
         * Da doc di tomcat: The standard protocol value for an AJP connector is
         * AJP/1.3 which uses an auto-switching mechanism to select either a
         * Java NIO based connector or an APR/native based connector. If the
         * PATH (Windows) or LD_LIBRARY_PATH (on most unix systems) environment
         * variables contain the Tomcat native library, the native/APR connector
         * will be used. If the native library cannot be found, the Java NIO
         * based connector will be used. To use an explicit protocol rather than
         * rely on the auto-switching mechanism described above, the following
         * values may be used: org.apache.coyote.ajp.AjpProtocol - blocking Java
         * connector org.apache.coyote.ajp.AjpNioProtocol - non blocking Java
         * NIO connector org.apache.coyote.ajp.AjpNio2Protocol - non blocking
         * Java NIO2 connector org.apache.coyote.ajp.AjpAprProtocol - the
         * APR/native connector
         */
        Connector ajpConnector = new Connector("org.apache.coyote.ajp.AjpNioProtocol");
        //ajpConnector.setProtocol("AJP/1.3");
        ajpConnector.setPort(ajpPort);
        ajpConnector.setSecure(false);
        ajpConnector.setAllowTrace(false);
        ajpConnector.setScheme("http");
        tomcat.addAdditionalTomcatConnectors(ajpConnector);
        return tomcat;
    }

    /**
     * crea il bean se setta il contesto JPA
     *
     * @return JPAServiceFactory
     */
    @Bean
    public JPAServiceFactory jPAServiceFactory() {
        return new JPAServiceFactory();
    }

    /**
     * imposta come rootLocator di Olingo i settaggi contenuti in
     * JPAServiceFactory
     *
     * @return OlingoRootLocator
     */
    @Bean
    public OlingoRootLocator getRootLocator() {
        OlingoRootLocator olingoRootLocator = new OlingoRootLocator();
        olingoRootLocator.setServiceFactory(jPAServiceFactory());
        return olingoRootLocator;
    }

    /**
     * crea una response di errore secondo il formato OData
     *
     * @return ODataExceptionMapperImpl
     */
    @Bean
    public ODataExceptionMapperImpl getExceptionHandler() {
        return new ODataExceptionMapperImpl();
    }

    @Bean
    public ODataApplication.MyProvider getProvider() {
        return new ODataApplication.MyProvider();
    }

    /**
     * CXF aiuta a sviluppare servizi come JAX-RS. Questi servizi possono
     * dialogare con molti protocolli come SOAP, XML/HTTP, RESTful HTTP, ecc...
     */
    /**
     * JAX-RS: Java API for RESTful Web Services (JAX-RS) fornisce supporto alla
     * creazione di web services in accordo con l'architettura REST. JAX-RS usa
     * annotazioni per semplificare lo sviluppo e il deploy di web service ed
     * endpoints
     *
     * @return JAXRSServerFactoryBean
     */
    @Bean
    @DependsOn("cxf")
    public Server oDataServer() {
        /**
         * JAXRSServerFactoryBean recupera la classe di servizio e per
         * reflection definisce l’infrastruttura per poter rispondere alle
         * richieste che il server riceverá (solo GET, ma anche il resto di
         * metodi esposti).
         */
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();

        sf.setServiceBeans(Arrays.<Object>asList(getRootLocator(), getExceptionHandler(), getProvider()));
        return sf.create();
    }
}
