package net.apnic.whowas.rdap;

import net.apnic.whowas.history.ObjectKey;

/**
 * AutNum RDAP object.
 */
public class AutNum
    extends GenericObject
{
    private static final long MAX_AUTNUM = 0xFFFFFFFFL;
    private static final long MIN_AUTNUM = 0x1L;

    private long endAutnum = 0L;
    private String handle = null;
    private long startAutnum = 0L;

    /**
     * Constructs a new autnum object with the given key.
     *
     * @param objectKey key for this object
     */
    public AutNum(ObjectKey objectKey)
    {
        super(objectKey);
    }

    /**
     * Provides the end autnum parameter used in constructing JSON response
     *
     * @return String representation of end autnum in this autnum object
     */
    public long getEndAutnum()
    {
        return endAutnum;
    }

    /**
     * {@inheritDoc}
     *
     * If not set this objects parent class implementation is used.
     */
    @Override
    public String getHandle()
    {
        if(handle == null)
        {
            return super.getHandle();
        }
        return handle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.AUTNUM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathHandle()
    {
        return Long.toString(getStartAutnum());
    }

    /**
     * Provides the start autnum parameter used in constructing JSON response
     *
     * @return String representation of start autnum in this autnum object
     */
    public long getStartAutnum()
    {
        return startAutnum;
    }

    /**
     * Sets the end autnum parameter for this object.
     *
     * @param endAutnum End autnum parameter
     */
    public void setEndAutnum(String endAutnum)
    {
        setEndAutnum(Long.parseLong(endAutnum));
    }

    /**
     * Sets the end autnum parameter for this object.
     *
     * @param endAutnum End autnum parameter
     */
    public void setEndAutnum(long endAutnum)
    {
        if(endAutnum < MIN_AUTNUM || endAutnum > MAX_AUTNUM)
        {
            throw new IllegalArgumentException("Invalid autnum");
        }
        this.endAutnum = endAutnum;
    }

    /**
     * Sets the handle parameter for this object.
     *
     * If not set this objects parent class implementation is used.
     *
     * @param handle Handle parameter
     */
    public void setHandle(String handle)
    {
        this.handle = handle;
    }

    /**
     * Sets the start autnum parameter for this object.
     *
     * @param startAutnum Start autnum parameter
     */
    public void setStartAutnum(String startAutnum)
    {
        try
        {
            setStartAutnum(Long.parseLong(startAutnum));
        }
        catch(Exception ex)
        {
            System.out.println("error " + startAutnum);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the start autnum parameter for this object.
     *
     * @param startAutnum Start autnum parameter
     */
    public void setStartAutnum(long startAutnum)
    {
        if(startAutnum < MIN_AUTNUM || startAutnum > MAX_AUTNUM)
        {
            throw new IllegalArgumentException("Invalid autnum");
        }
        this.startAutnum = startAutnum;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return String.format("autnum: %s - %s", getStartAutnum(),
            getEndAutnum());
    }
}
