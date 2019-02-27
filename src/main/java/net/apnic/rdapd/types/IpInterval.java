package net.apnic.rdapd.types;

import net.apnic.rdapd.intervaltree.Interval;
import net.ripe.ipresource.IpAddress;
import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.Ipv4Address;
import net.ripe.ipresource.Ipv6Address;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * ATTENTION: this only works for rounded intervals (i.e. won't work for "10.0.0.50 - 10.0.0.255")
     */
    public String toCIDRString()
    {
        return low().toString() + CIDR_DELIMITER + prefixSize();
    }

    /**
     * Splits this interval to prefixes (that can be represented by the CIDR notation). If the interval is already a
     * prefix, it will return a {@link Set} containing a copy of the same interval.
     * @return a {@link Set} with the prefixes contained in the interval
     */
    public Set<IpInterval> splitToPrefixes() {
        IpAddress low, high;

        if (low().getAddressFamily() == IP.AddressFamily.IPv4) {
            low = new Ipv4Address(new BigInteger(low().getAddress().getAddress()).longValue());
            high = new Ipv4Address(new BigInteger(high().getAddress().getAddress()).longValue());
        } else { // IPv6
            low = new Ipv6Address(new BigInteger(low().getAddress().getAddress()));
            high = new Ipv6Address(new BigInteger(high().getAddress().getAddress()));
        }

        return IpRange.range(low, high)
                .splitToPrefixes().stream()
                .map(r -> {
                        try {
                            return new IpInterval(
                                    new IP(InetAddress.getByAddress(r.getStart().getValue().toByteArray())),
                                    r.getPrefixLength());
                        } catch (UnknownHostException e) {
                            // only valid prefixes will be provided, so this exception won't be happening
                            throw new RuntimeException(e);
                        }
                    })
                .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return low().getAddressFamily() == IP.AddressFamily.IPv4 ?
            low().toString() + RANGE_DELIMITER + high().toString() :
            low().toString() + CIDR_DELIMITER + prefixSize();
    }
}
