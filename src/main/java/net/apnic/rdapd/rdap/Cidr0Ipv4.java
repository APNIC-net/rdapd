package net.apnic.rdapd.rdap;

/**
 * CIDR representation for IPv4 according to NRO's
 * <a href="https://gitlab.nro.net/ecg/draft-rdap-cidr">CIDR Expressions in RDAP</a>.
 */
public class Cidr0Ipv4 implements Cidr0Object {
    private final String v4prefix;
    private final int length;

    public Cidr0Ipv4(String v4prefix, int length) {
        this.v4prefix = v4prefix;
        this.length = length;
    }

    public String getV4prefix() {
        return v4prefix;
    }

    public int getLength() {
        return length;
    }
}