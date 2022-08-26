
package it.bologna.ausl.gipi.pubblicazioni;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */
public interface Marshaller {

    public static <T extends Marshaller> T parse(String value, Class<T> requestableClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper.readValue(value, requestableClass);
    }

    @JsonIgnore
    public static <T extends Marshaller> T parse(InputStream value, Class<T> requestableClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper.readValue(value, requestableClass);
    }

    @JsonIgnore
    public default String getJSONString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        String writeValueAsString = mapper.writeValueAsString(this);
        return writeValueAsString;
    }
}

