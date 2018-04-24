package net.apnic.rdapd.rdap.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import net.apnic.rdapd.rdap.controller.RequestContext;
import net.apnic.rdapd.rdap.Link;
import net.apnic.rdapd.rdap.RelativeLink;

public class LinkSerializer
    extends StdSerializer<Link>
{
    private static final long serialVersionUID = -12L;

    private transient final RequestContext context;

    public LinkSerializer(Supplier<HttpServletRequest> request)
    {
        super(Link.class);
        this.context = new RequestContext(request);
    }

    @Override
    public void serialize(Link value, JsonGenerator gen,
        SerializerProvider provider)
        throws IOException
    {
        gen.writeStartObject();
        gen.writeStringField("value", context.getContext().toString());
        gen.writeStringField("rel", value.getRel());
        gen.writeStringField("href",
            value instanceof RelativeLink ?
                context.getReferenceWithSpec(value.getHref()).toString() :
                value.getHref());
        gen.writeStringField("type", value.getType());
        gen.writeEndObject();
    }
}
