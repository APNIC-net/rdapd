package net.apnic.whowas.rdap.controller;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for RDAP endpoint to set the current thread local context for
 * a request.
 */
public class RequestContext
{
    private static ThreadLocal<URL> threadContext;

    static
    {
        threadContext = new ThreadLocal();
    }

    public static void setContext(HttpServletRequest request)
        throws MalformedURLException
    {
        threadContext.set(new URL(request.getRequestURL().toString()));
    }

    public static URL getContext()
    {
        return threadContext.get();
    }

    public static URL getReference()
    {
        URL context = threadContext.get();
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

    public static URL getReferenceWithSpec(String spec)
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
