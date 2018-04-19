package it.bologna.ausl.gipi.odata.processor;

import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomOdataDebugCallback implements ODataErrorCallback {

    private static final Logger log = LoggerFactory.getLogger(CustomOdataDebugCallback.class);

    @Override
    public ODataResponse handleError(ODataErrorContext context) throws ODataApplicationException {
        context.getException().printStackTrace();
        log.error(context.getException().getClass().getName() + ":" + context.getMessage());
        return EntityProvider.writeErrorDocument(context);
    }
}
