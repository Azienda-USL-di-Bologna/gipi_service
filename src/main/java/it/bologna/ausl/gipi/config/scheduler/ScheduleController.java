package it.bologna.ausl.gipi.config.scheduler;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author spritz
 */
@RestController
//@RequestMapping(value = "test")
@RequestMapping(value = "${utility.mapping.url.root}" + "/scheduler")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    ServiceManager serviceManager;

    @RequestMapping(value = {"start/{name}", "start/{name}/{idazienda}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> start(
            @PathVariable(required = true) String name,
            @PathVariable(required = false) Integer idazienda,
            HttpServletRequest request) {
        String res = String.format("servizio %s ", name);

        try {
            serviceManager.startService(new ServiceKey(name, idazienda));
            res += "avviato";
        } catch (Exception ex) {
            res += "non presente: " + ex;
        }

        return ResponseEntity.ok(res);
    }

    @RequestMapping(value = {"stop/{name}", "stop/{name}/{idazienda}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> stop(
            @PathVariable(required = true) String name,
            @PathVariable(required = false) Integer idazienda,
            HttpServletRequest request) {

        String res = String.format("servizio %s ", name);

        try {
            serviceManager.stopService(new ServiceKey(name, idazienda));
            res += "fermato";
        } catch (Exception ex) {
            res += "non presente: " + ex;
        }

        return ResponseEntity.ok(res);
    }

    @RequestMapping(value = {"reload"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reload() {

        serviceManager.reloadFromDb();
        return ResponseEntity.ok(String.format("servizi ricaricati da database"));
    }

    @RequestMapping(value = {"stopall"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> stopAllServices() {

        serviceManager.stopAllService();
        return ResponseEntity.ok("tutti i servizi sono stati fermati");
    }
}
