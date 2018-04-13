package net.apnic.rdapd.rdap.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Utility class for RDAP endpoint to set the current thread local context for
 * a request.
 */
public class RequestContext
{
    public static String getContext()
    {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        try
        {
            return builder.build().toString();
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static String getReferenceWithSpec(String spec)
    {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        try
        {
            return builder.replacePath(spec).replaceQuery("").build().toString();
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
