package it.bologna.ausl.gipi.config.olingo;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfServletRegister2 {

    /**
     * Indirizzo base del servizio Olingo (definizione nel file
     * application.properties)
     */
    @Value("${odata.mapping.url.root}")
    private String odataMappingUrlRoot;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(CxfServletRegister2.class, args);
    }

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
