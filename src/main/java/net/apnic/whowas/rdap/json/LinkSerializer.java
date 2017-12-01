package net.apnic.whowas.rdap.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import net.apnic.whowas.rdap.controller.RequestContext;
import net.apnic.whowas.rdap.Link;
import net.apnic.whowas.rdap.RelativeLink;

public class LinkSerializer
    extends StdSerializer<Link>
{
    public LinkSerializer()
    {
        super(Link.class);
    }

    @Override
    public void serialize(Link value, JsonGenerator gen,
        SerializerProvider provider)
        throws IOException
    {
        gen.writeStartObject();
        gen.writeStringField("value", RequestContext.getContext().toString());
        gen.writeStringField("rel", value.getRel());
        gen.writeStringField("href",
            value instanceof RelativeLink ?
                RequestContext.getReferenceWithSpec(value.getHref()).toString() :
                value.getHref());
        gen.writeStringField("type", value.getType());
        gen.writeEndObject();
    }
}
