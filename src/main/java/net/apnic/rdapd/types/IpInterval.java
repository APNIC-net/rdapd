package net.apnic.rdapd.types;

import net.apnic.rdapd.intervaltree.Interval;

import java.io.Serializable;

public class IpInterval implements Interval<IP>, Serializable {
    private static final String CIDR_DELIMITER = "/";
    private static final String RANGE_DELIMITER = "-";

    private final IP low, high;

    public IpInterval(IP low, IP high) {
        this.low = low;
        this.high = high;
    }

    public IpInterval(IP low, int prefixLength) {
        this.low = low;
        this.high = low.addPrefix(prefixLength).get();  // or runtime exception
    }

    /**
     * Return the (rounded up) prefix size of this range.
     *
     * This the base two logarithm of the number of addresses in the range subtracted from the base two logarithm
     * of the address size.
     *
     * @return the prefix size of the range
     */
    public int prefixSize() {
        return low.prefixFrom(high()).orElse(128);
    }

    @Override
    public IP low() {
        return low;
    }

    @Override
    public IP high() {
        return high;
    }

    public String toCIDRString()
    {
        return low().toString() + CIDR_DELIMITER + prefixSize();
    }

    @Override
    public String toString()
    {
        return low().getAddressFamily() == IP.AddressFamily.IPv4 ?
            low().toString() + RANGE_DELIMITER + high().toString() :
            low().toString() + CIDR_DELIMITER + prefixSize();
    }
}
