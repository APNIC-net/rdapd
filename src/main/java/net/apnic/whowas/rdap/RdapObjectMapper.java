package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.jackson.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.apnic.whowas.rdap.Patches.FixedStructuredValueSerializer;

/**
 * The be.dnsbelgium.rdap.jackson.CustomObjectMapper class does not allow
 * for a different JsonFactory, so this is a copy of that class, which does.
 *
 * A few default settings are also changed, and custom serializers are added.
 *
 **/
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

        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addSerializer(new RDAPContactSerializer());
        simpleModule.addSerializer(new FixedStructuredValueSerializer());
        simpleModule.addSerializer(new TextListSerializer());
        simpleModule.addSerializer(new TextSerializer());
        simpleModule.addSerializer(new URIValueSerializer());
        simpleModule.addSerializer(new DomainNameSerializer());
        simpleModule.addSerializer(new DateTimeSerializer());
        simpleModule.addSerializer(new StatusSerializer());

        registerModule(simpleModule);

        findAndRegisterModules();
    }
}
