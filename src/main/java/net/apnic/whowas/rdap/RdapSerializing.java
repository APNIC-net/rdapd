package net.apnic.whowas.rdap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import java.io.IOException;
import java.io.ObjectStreamException;

/**
 * Abstraction of an RDAP object
 */
final class RdapSerializing {
    private static final ObjectMapper MAPPER = new RdapObjectMapper(new SmileFactory());
    private RdapSerializing() {
    }

    static <T> byte[] serialize(T thing) throws JacksonException {
        try {
            return MAPPER.writeValueAsBytes(thing);
        } catch (JsonProcessingException ex) {
            throw new JacksonException(ex);
        }
    }

    static <T> T deserialize(byte[] data, Class<T> tClass) {
        try {
            return MAPPER.readValue(data, tClass);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    final static class JacksonException extends ObjectStreamException {
        private JacksonException(JsonProcessingException cause) {
            super(cause.getLocalizedMessage());
        }
    }
}
