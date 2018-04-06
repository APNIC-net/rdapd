package net.apnic.rdapd.rdap.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RelatedEntityFilter
    extends SimpleBeanPropertyFilter
{
    public RelatedEntityFilter() {}

    @Override
    protected boolean include(BeanPropertyWriter writer)
    {
        return true;
    }

    @Override
    protected boolean include(PropertyWriter writer)
    {
        return true;
    }

    private String getGrandparentContextName(JsonGenerator jgen)
    {
        JsonStreamContext jsonStreamContext = jgen.getOutputContext();
        JsonStreamContext parentContext = jsonStreamContext.getParent();
        if (parentContext == null) {
            return null;
        }
        JsonStreamContext grandparentContext = parentContext.getParent();
        if (grandparentContext == null) {
            return null;
        }
        return grandparentContext.getCurrentName();
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen,
        SerializerProvider provider, PropertyWriter writer)
        throws Exception
    {
        /* Do not include nested entities in responses. */
        if (writer.getName().equals("entities")) {
            String grandparentContextName = getGrandparentContextName(jgen);
            if (grandparentContextName != null
                    && grandparentContextName.equals("entities")) {
                return;
            }
        }

        writer.serializeAsField(pojo, jgen, provider);
    }
}
