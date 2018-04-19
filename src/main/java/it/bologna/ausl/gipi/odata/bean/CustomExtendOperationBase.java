package it.bologna.ausl.gipi.odata.bean;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;

/**
 * Created by f.longhitano on 23/06/2017.
 */
public class CustomExtendOperationBase implements WithODataJPAContext {

    private ODataJPAContext oDataJPAContext;

    @Override
    public ODataJPAContext getoDataJPAContext() {
        return oDataJPAContext;
    }

    @Override
    public void setoDataJPAContext(ODataJPAContext oDataJPAContext) {
        this.oDataJPAContext = oDataJPAContext;
    }
}
