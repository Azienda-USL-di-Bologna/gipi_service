package it.bologna.ausl.gipi.odata.processor;

//import it.nextsw.entities.Utente;
import it.bologna.ausl.gipi.odata.bean.CustomExtendOperationBase;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
//@EdmFunctionImportClass
public class CustomRestProcessor extends CustomExtendOperationBase {

    private static final Logger log = LoggerFactory.getLogger(CustomRestProcessor.class);

    @PersistenceContext
    private EntityManager em;
//    @Autowired
//    private UtenteRepository utenteRepository;

    //    @EdmFunctionImport(
//            name = "TransazioneConAnnotation",
//            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.SIMPLE),
//            httpMethod = EdmFunctionImport.HttpMethod.POST)
//    @Transactional
//    public String provaTransazioneConAnnotation(
//            @EdmFunctionImportParameter(name = "nameContains", facets = @EdmFacets(nullable = false)) final String nameToFind,
//            @EdmFunctionImportParameter(name = "replacing", facets = @EdmFacets(nullable = false)) final String replacingName
//    ) {
//        QUtenti utenti = QUtenti.utenti;
//        // ottimizzata se usiamo Eclipselink come jpa provider (togliere il secondo parametro altrimenti)
//        JPQLQuery query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
//
//         ottengo gli utenti che matchano con quel nome utente
//        List<Utenti> usersList = query.from(utenti)
//                .orderBy(utenti.nome.desc())
//                .where(utenti.nome.contains(nameToFind))
//                .list(utenti);
//        if (usersList.size() > 1) {
//
//            usersList.get(0).setCognome(replacingName);
//
//        return "Check FI con POST";
//    }
//
//    @EdmFunctionImport(
//            name = "pippo",
//            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.COMPLEX, isCollection = true),
//            httpMethod = EdmFunctionImport.HttpMethod.GET
//    )
//    public List<OrderValue> getOrderValue(
//            @EdmFunctionImportParameter(name = "nameContains", facets = @EdmFacets(nullable = true))
//                    String nameToFind
//    ) {
//        logger.info("sono nella pippo");
//        List<OrderValue> orderValues = new ArrayList<>();
//        OrderValue orderValue;
//        for (int i = 0; i < 100; i++) {
//            orderValue = new OrderValue();
//            orderValue.setAmount(1.5d * i);
//            orderValues.add(orderValue);
//            orderValue.setCurrency("SOFFITEIR" + i);
//        }
//        return orderValues;
//    }
//    @EdmFunctionImport(
//            name = "UtentiNome",
//            returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.ENTITY, isCollection = true),
//            httpMethod = EdmFunctionImport.HttpMethod.GET,
//            entitySet = "Utentes"
//    )
//    public List<Utente> getFuncUtente(
//            @EdmFunctionImportParameter(name = "nomeutente", facets = @EdmFacets(nullable = true))
//                    String nome,
//            @EdmFunctionImportParameter(name = "skip", facets = @EdmFacets(nullable = true))
//                    Integer skip,
//            @EdmFunctionImportParameter(name = "top", facets = @EdmFacets(nullable = true))
//                    Integer top
//    ) {
//        try {
////            JPAQuery<Utente> query = new JPAQuery<>(em);
////            QUtente utenteRicerca = QUtente.utente;
////            List<Utente> utenteTrovato = query.from(utenteRicerca)
////                    .where(utenteRicerca.nome.eq(nome)).offset(skip).limit(top).fetch();
////            return utenteTrovato;
//            return null;
//        } catch (Exception e) {
//            System.out.println("Eccezione nella function: " + e.getMessage());
//            return null;
//        }
//    }
}
