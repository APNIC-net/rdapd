package net.apnic.whowas.rdap;

import net.apnic.whowas.history.ObjectKey;

/**
 * AutNum RDAP object.
 */
public class AutNum
    extends GenericObject
{
    private String endAutnum = null;
    private String handle = null;
    private String startAutnum = null;

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
    public String getEndAutnum()
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
        return getStartAutnum();
    }

    /**
     * Provides the start autnum parameter used in constructing JSON response
     *
     * @return String representation of start autnum in this autnum object
     */
    public String getStartAutnum()
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
