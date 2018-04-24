
package net.apnic.rdapd.autnum;

import net.apnic.rdapd.intervaltree.Interval;

import java.io.Serializable;

public class ASNInterval
    implements Interval<ASN>, Serializable
{
    private final ASN high;
    private final ASN low;

    public ASNInterval(long low, long high)
    {
        this(new ASN(low), new ASN(high));
    }

    public ASNInterval(ASN low, ASN high)
    {
        if(low.compareTo(high) > 0)
        {
            throw new IllegalArgumentException("ASNInterval low <= high");
        }
        this.low = low;
        this.high = high;
    }

    public ASN high()
    {
        return high;
    }

    public ASN low()
    {
        return low;
    }

    @Override
    public String toString()
    {
        return String.format("%s - %s", low(), high());
    }
}
