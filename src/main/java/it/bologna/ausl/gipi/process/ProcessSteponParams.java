package it.bologna.ausl.gipi.process;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author user
 */
public class ProcessSteponParams {

    private Map<String, Object> params;

    public ProcessSteponParams() {
        params = new HashMap<String, Object>();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    // metodi per inserire o leggere un parametro
    public Object readParam(String key) {
        return params.get(key);
    }

    public void insertParam(String key, Object value) {
        params.put(key, value);
    }
}
