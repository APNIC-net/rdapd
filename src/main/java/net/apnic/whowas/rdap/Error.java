package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.List;

import net.apnic.whowas.history.ObjectKey;

public class Error
    implements RdapObject
{
    public static final Error MALFORMED_REQUEST = new Error(400,
        "Malformed Request", Arrays.asList("Unable to understand request"));

    public static final Error NOT_FOUND = new Error(404, "Not Found",
        Arrays.asList("The server has not found anything matching the Request-URI."));

    public static final Error NOT_IMPLEMENTED = new Error(501, "Not Implemented",
        Arrays.asList("This type of request is not supported by this server."));

    public static final Error SERVER_EXCEPTION = new Error(500, "Internal Server Error",
        Arrays.asList("Error processing request"));

    private List<String> description = null;
    private int errorCode;
    private String title;

    public Error(int errorCode, String title, List<String> description)
    {
        this.description = description;
        this.errorCode = errorCode;
        this.title = title;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getDescription()
    {
        return description;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    @Override
    public ObjectKey getObjectKey()
    {
        return null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getTitle()
    {
        return title;
    }
}
