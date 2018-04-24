package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Set up object mapper configuration values.
 */
public final class RdapObjectMapper extends ObjectMapper {
    public RdapObjectMapper() {
        this(new JsonFactory());
    }

    public RdapObjectMapper(JsonFactory jsonFactory) {
        super(jsonFactory);
        super.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        findAndRegisterModules();
    }
}
