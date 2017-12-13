package it.bologna.ausl.gipi.process;

import java.util.Map;

/**
 *
 * @author user
 */
public class SteponParams {

    private Map<String, Object> params;

    public Object getParam(String key) {
        return params.get(key);
    }

    public void putParam(String key, Object value) {
        params.put(key, value);
    }

}
