package it.bologna.ausl.gipi.config.olingo;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfServletRegister {

    /**
     * Indirizzo base del servizio Olingo (definizione nel file
     * application.properties)
     */
    @Value("${odata.mapping.url.root}")
    private String odataMappingUrlRoot;

    /**
     * Viene registrata la servlet che risponderà all'indirizzo base del
     * servizio Olingo
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean cxfServletRegistrationBean() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new CXFServlet(), odataMappingUrlRoot);

        registrationBean.setName("CXFServlet");

        return registrationBean;
    }
}
