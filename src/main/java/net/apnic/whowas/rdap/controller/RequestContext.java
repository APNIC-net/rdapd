package net.apnic.whowas.rdap.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for RDAP endpoint to set the current thread local context for
 * a request.
 */
public class RequestContext
{
    private final Supplier<HttpServletRequest> request;

    public RequestContext(Supplier<HttpServletRequest> request)
    {
        this.request = request;
    }

    public URL getContext()
    {
        try
        {
            return new URL(request.get().getRequestURL().toString());
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public URL getReference()
    {
        URL context = getContext();
        try
        {
            return new URL(context.getProtocol(), context.getHost(),
                context.getPort(), "");
        }
        catch(MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public URL getReferenceWithSpec(String spec)
    {
        try
        {
            return new URL(getReference(), spec);
        }
        catch(MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
