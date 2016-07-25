package net.apnic.whowas.loader.Types;

import net.apnic.whowas.loader.IntervalTree.Interval;

public class IpInterval implements Interval<IP> {
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
}
