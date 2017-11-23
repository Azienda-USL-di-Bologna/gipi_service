package it.bologna.ausl.gipi.controller;

import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

@RestController
@RequestMapping("/gipi/resources/custom/")
@PropertySource("classpath:query.properties")
public class ProcedimentiController {

    @Autowired
    private Sql2o sql2o;

    @Autowired
    private EntityManager em;

    @Value("${functionimports.query-strutture-con-check}")
    private String queryStruttureText;

    @RequestMapping(value = "updateProcedimenti", method = RequestMethod.POST)
    public void updateProcedimenti(
            @RequestBody Map<String, Object> data
    ) {

        Map<Integer, String> nodeInvolved = (Map<Integer, String>) data.get("nodeInvolved");
        Integer idAziendaTipoProcedimento = (Integer) data.get("idAziendaTipoProcedimento");

        if (nodeInvolved != null && !nodeInvolved.isEmpty()) {
            em.getTransaction().begin();

            for (Map.Entry<Integer, String> entry : nodeInvolved.entrySet()) {
                Integer idStruttura = entry.getKey();
                String operation = entry.getValue();

                if (operation.equalsIgnoreCase("INSERT")) {
                    Procedimento p = new Procedimento();

                    p.setDataInizio(new Date());
                    p.setIdAziendaTipoProcedimento(new AziendaTipoProcedimento((Integer) data.get("idAziendaTipoProcedimento")));
                    p.setIdStruttura(new Struttura(idStruttura));
                    em.persist(p);

                } else if (operation.equalsIgnoreCase("DELETE")) {

                } else {

                }

            }
        }

        // oppure  em.remove(employee);
        em.getTransaction().commit();

    }

}
