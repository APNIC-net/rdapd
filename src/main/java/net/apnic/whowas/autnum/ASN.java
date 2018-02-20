
package net.apnic.whowas.autnum;

public class ASN
    implements Comparable<ASN>
{
    private static final long MAX_ASN = 0xFFFFFFFFL;
    private static final long MIN_ASN = 0x1L;
    private long asn = 0;

    public ASN(long asn)
    {
        if(asn < MIN_ASN || asn > MAX_ASN)
        {
            throw new IllegalArgumentException(
                String.format("invalid ASN value must be in the range %d - %d",
                    MIN_ASN, MAX_ASN));
        }
        this.asn = asn;
    }

    @Override
    public int compareTo(ASN o)
    {
        if(getASN() == o.getASN())
        {
            return 0;
        }
        return getASN() < o.getASN() ? -1 : 1;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof ASN && getASN() == ((ASN)other).getASN();
    }

    public long getASN()
    {
        return asn;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getASN()).hashCode();
    }
}
