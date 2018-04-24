package net.apnic.rdapd.autnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ASNTest
{
    @Test
    public void canConstructASN()
    {
        ASN asn1 = new ASN(1234);
        assertEquals(1234, asn1.getASN());

        ASN asn2 = new ASN(ASN.MIN_ASN);
        assertEquals(ASN.MIN_ASN, asn2.getASN());

        ASN asn3 = new ASN(ASN.MAX_ASN);
        assertEquals(ASN.MAX_ASN, asn3.getASN());

        ASN asn4 = ASN.valueOf(1);
        assertEquals(1, asn4.getASN());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForBadArgument1()
    {
        new ASN(ASN.MIN_ASN - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForBadArgument2()
    {
        new ASN(ASN.MAX_ASN + 1);
    }

    @Test
    public void asnEquality()
    {
        assertTrue(new ASN(1234).equals(new ASN(1234)));
        assertFalse(new ASN(1234).equals(new ASN(1235)));
    }

    @Test
    public void asnEquivalence()
    {
        assertTrue(ASN.valueOf(1234).compareTo(ASN.valueOf(1234)) == 0);
        assertTrue(ASN.valueOf(1234).compareTo(ASN.valueOf(1235)) < 0);
        assertTrue(ASN.valueOf(1234).compareTo(ASN.valueOf(1233)) > 0);
    }
}
