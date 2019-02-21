package net.apnic.rdapd.rdap;

/**
 * CIDR representation for IPv6 according to NRO's
 * <a href="https://gitlab.nro.net/ecg/draft-rdap-cidr">CIDR Expressions in RDAP</a>.
 */
public class Cidr0Ipv6 implements Cidr0Object {
    private final String v6prefix;
    private final String length;

    public Cidr0Ipv6(String v6prefix, String length) {
        this.v6prefix = v6prefix;
        this.length = length;
    }

    public String getV6prefix() {
        return v6prefix;
    }

    public String getLength() {
        return length;
    }
}