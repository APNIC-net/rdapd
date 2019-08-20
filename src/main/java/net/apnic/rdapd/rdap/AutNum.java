package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.apnic.rdapd.autnum.ASNInterval;
import net.apnic.rdapd.history.ObjectKey;

/**
 * AutNum RDAP object.
 */
public class AutNum
    extends GenericObject
{
    private ASNInterval asnInterval = null;
    private String handle = null;

    /**
     * Constructs a new autnum object with the given key.
     *
     * @param objectKey key for this object
     */
    public AutNum(ObjectKey objectKey)
    {
        super(objectKey);
    }

    @JsonIgnore
    public ASNInterval getASNInterval()
    {
        return asnInterval;
    }

    /**
     * Provides the end autnum parameter used in constructing JSON response
     *
     * @return String representation of end autnum in this autnum object
     */
    public long getEndAutnum()
    {
        return asnInterval.high().getASN();
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
        return asnInterval.low().getASN();
    }

    public void setASNInterval(String startAutnum, String endAutnum)
    {
        setASNInterval(asnStringToLong(startAutnum), asnStringToLong(endAutnum));
    }

    public void setASNInterval(long startAutnum, long endAutnum)
    {
        asnInterval = new ASNInterval(startAutnum, endAutnum);
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

    private static Long asnStringToLong(String autnum) {
        return Long.parseLong(autnum.replaceAll("[aA][sS]", ""));
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
