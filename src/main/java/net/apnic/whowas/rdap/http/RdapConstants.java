package net.apnic.whowas.rdap.http;

import org.springframework.http.MediaType;

public class RdapConstants
{
    public static final MediaType RDAP_MEDIA_TYPE =
        new MediaType("application", "rdap+json");

    /**
     * Cannot construct this class
     */
    private RdapConstants()
    {
    }
}
