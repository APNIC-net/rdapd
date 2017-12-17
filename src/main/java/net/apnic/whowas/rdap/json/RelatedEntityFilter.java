package net.apnic.whowas.rdap.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RelatedEntityFilter
    extends SimpleBeanPropertyFilter
{
    private static final int MAX_DEPTH = 1;

    private final ThreadLocal<Integer> entityDepthCount;

    public RelatedEntityFilter()
    {
        entityDepthCount = new ThreadLocal<Integer>()
        {
            @Override
            protected Integer initialValue()
            {
                return 0;
            }
        };
    }
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

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen,
        SerializerProvider provider, PropertyWriter writer)
        throws Exception
    {
        if(writer.getName().equals("entities") && entityDepthCount.get().equals(MAX_DEPTH))
        {
            entityDepthCount.set(0);
            return;
        }
        else if(writer.getName().equals("entities"))
        {
            entityDepthCount.set(entityDepthCount.get().intValue() + 1);
        }
        writer.serializeAsField(pojo, jgen, provider);
    }
}
