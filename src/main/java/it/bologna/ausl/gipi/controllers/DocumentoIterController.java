package it.bologna.ausl.gipi.controllers;

import com.querydsl.core.types.Predicate;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.repository.DocumentoIterRepository;
import it.bologna.ausl.entities.utilities.response.controller.ControllerHandledExceptions;
import it.bologna.ausl.entities.utilities.response.exceptions.BadRequestResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.ConflictResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.NotFoundResponseException;
import it.bologna.ausl.gipi.odata.interceptor.DocumentoIterInterceptor;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author gdm
 */
@RestController
@RequestMapping(value = "${custom.mapping.url.root}" + "/documento-iter")
public class DocumentoIterController extends ControllerHandledExceptions {
    
    private final Integer NOT_FOUND_ERROR_CODE = 0;
    private final Integer NO_DELETE_ERROR_CODE = 1;
    
    @Autowired
    DocumentoIterRepository documentoIterRepository;
    
    @Autowired
    DocumentoIterInterceptor documentoIterInterceptor;
    
    @PersistenceContext
    EntityManager em;
    
    @RequestMapping(method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void delete(
            @QuerydslPredicate(root = DocumentoIter.class) Predicate predicate) {
        
        if (predicate == null) {
            throw new BadRequestResponseException(NOT_FOUND_ERROR_CODE, "non è stato specificata l'associazione documento-iter da cancellare", "");
        }
        Iterable<DocumentoIter> documentiIter = documentoIterRepository.findAll(predicate);
        if (documentiIter == null || !documentiIter.iterator().hasNext()) {
            throw new NotFoundResponseException(NOT_FOUND_ERROR_CODE, "associazione documento iter non trovata", "associazione documento iter non trovata");
        }
        for (DocumentoIter documentoIter: documentiIter) {
            try {
                documentoIterInterceptor.onDeleteInterceptor(documentoIter, em, null);
                documentoIterRepository.delete(documentoIter);
            }
            catch (OlingoRequestRollbackException ex) {
                throw new ConflictResponseException(NO_DELETE_ERROR_CODE, "eliminazione bloccata", ex.getMessage());
            }
        }
    }
}