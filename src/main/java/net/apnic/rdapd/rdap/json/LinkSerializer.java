package net.apnic.rdapd.rdap.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import net.apnic.rdapd.rdap.controller.RequestContext;
import net.apnic.rdapd.rdap.Link;
import net.apnic.rdapd.rdap.RelativeLink;

public class LinkSerializer
    extends StdSerializer<Link>
{
    private static final long serialVersionUID = -12L;

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
        gen.writeStringField("value", RequestContext.getContext());
        gen.writeStringField("rel", value.getRel());
        gen.writeStringField("href",
            value instanceof RelativeLink ?
                RequestContext.getReferenceWithSpec(value.getHref()) :
                value.getHref());
        gen.writeStringField("type", value.getType());

        if (value.getHreflang() != null) {
            gen.writeStringField("hreflang", value.getHreflang());
        }

        gen.writeEndObject();
    }
}
