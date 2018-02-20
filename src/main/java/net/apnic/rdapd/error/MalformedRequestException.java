package net.apnic.rdapd.error;

/**
 * Exception for malformed RDAP path segment requests.
 */
public class MalformedRequestException
    extends RuntimeException
{
    /**
     * Default constructor
     */
    public MalformedRequestException()
    {
        super();
    }

    /**
     * Constructs a new MalformedRequestException from that of another
     * throwable.
     *
     * @see RuntimeException
     */
    public MalformedRequestException(Throwable ex)
    {
        super(ex);
    }
}
