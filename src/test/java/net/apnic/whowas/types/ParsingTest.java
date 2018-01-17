package net.apnic.whowas.types;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ParsingTest
{
    @Test
    public void testParseAutnum() throws Exception {
        Stream.of("123",
                  "1",
                  "65535",
                  "65536",
                  "4294967295")
        .forEach(validAutnum ->
        {
            Parsing.parseAutnum(validAutnum);
        });
    }

    @Test
    public void testBadAutnumParsing()
    {
        Stream.of("AS1234",
                  "-1234",
                  "0",
                  "4294967296")
        .forEach(badAutnum ->
        {
            try
            {
                Parsing.parseAutnum(badAutnum);
                assertTrue("Did not thrown exception for " + badAutnum, false);
            }
            catch(RuntimeException ex)
            {
                assertTrue(true);
            }
        });
    }

    @Test
    public void testParseInterval() throws Exception {
        Parsing.parseInterval("202.12.29.0");
        Parsing.parseInterval("202.12.29.0- 202.12.29.255");
        Parsing.parseInterval("202.12.29.0  \t-   202.12.29.255");
        Parsing.parseInterval("202.12.29.0/24");
        Parsing.parseInterval("2403:AD00:1000::/48");
        Parsing.parseInterval("::");
        Parsing.parseInterval("2403:ad00:1000::1");
        Parsing.parseInterval("::2004");
        Parsing.parseInterval("::3");
        Parsing.parseInterval("::/0");
        Parsing.parseInterval("::/128");
        Parsing.parseInterval("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128");
        Parsing.parseInterval("7fff::/64");
        Parsing.parseInterval("7fff::/63");
        Parsing.parseInterval("7fff::/127");
        Parsing.parseInterval("7fff::/128");
    }

    @Test
    public void testBadIntervalRangeFails() {
        Stream.of("1", "1.2", "1.2.3", "1.2.3.", "255.255.255.-126",
                  "2004:", "2004:2004", "::2004f", "2404:8400:ffff::/36",
                  "ff:ff:ff:ff:ff:ff:ff:ff:ff", "ff:ff:ff:ff:ff:ff:ff:ff::",
                  "7fff::/2")
        .forEach(nonAddress ->
        {
            try
            {
                Parsing.parseInterval(nonAddress);
                assertTrue("Did not thrown exception for " + nonAddress, false);
            }
            catch(RuntimeException ex)
            {
                assertTrue(true);
            }
        });
    }

}
