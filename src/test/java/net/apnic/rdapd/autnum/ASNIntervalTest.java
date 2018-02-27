package net.apnic.rdapd.autnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ASNIntervalTest
{
    @Test
    public void canConstructASNInterval()
    {
        ASNInterval asnRng1 = new ASNInterval(1, 2);
        assertEquals(asnRng1.low().getASN(), 1);
        assertEquals(asnRng1.high().getASN(), 2);

        ASNInterval asnRng2 = new ASNInterval(ASN.MIN_ASN, ASN.MAX_ASN);
        assertEquals(asnRng2.low().getASN(), ASN.MIN_ASN);
        assertEquals(asnRng2.high().getASN(), ASN.MAX_ASN);

        ASNInterval asnRng3 = new ASNInterval(ASN.valueOf(ASN.MIN_ASN), ASN.valueOf(ASN.MAX_ASN));
        assertEquals(asnRng3.low().getASN(), ASN.MIN_ASN);
        assertEquals(asnRng3.high().getASN(), ASN.MAX_ASN);

        ASNInterval asnRng4 = new ASNInterval(1, 1);
        assertEquals(asnRng4.low().getASN(), 1);
        assertEquals(asnRng4.high().getASN(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForBadArgument1()
    {
        new ASNInterval(ASN.MAX_ASN, ASN.MIN_ASN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForBadArgument2()
    {
        new ASNInterval(ASN.MIN_ASN - 1, ASN.MAX_ASN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForBadArgument3()
    {
        new ASNInterval(ASN.MIN_ASN, ASN.MAX_ASN + 1);
    }

    @Test
    public void asnIntervalEquivalence()
    {
        ASNInterval asni1 = new ASNInterval(1, 10);
        ASNInterval asni2 = new ASNInterval(1, 3);
        assertTrue(asni1.compareTo(asni2) < 0);

        asni1 = new ASNInterval(1, 10);
        asni2 = new ASNInterval(2, 8);
        assertTrue(asni1.compareTo(asni2) < 0);

        asni1 = new ASNInterval(1, 10);
        asni2 = new ASNInterval(1, 10);
        assertTrue(asni1.compareTo(asni2) == 0);

        asni1 = new ASNInterval(1, 2);
        asni2 = new ASNInterval(1, 10);
        assertTrue(asni1.compareTo(asni2) > 0);
    }
}
