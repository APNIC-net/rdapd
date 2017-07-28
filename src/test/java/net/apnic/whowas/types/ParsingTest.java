package net.apnic.whowas.types;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ParsingTest {

    @Test
    public void testParseInterval() throws Exception {
        Parsing.parseInterval("202.12.29.0");
        Parsing.parseInterval("202.12.29.0- 202.12.29.255");
        Parsing.parseInterval("202.12.29.0  \t-   202.12.29.255");
        Parsing.parseInterval("202.12.29.0/24");
        Parsing.parseInterval("2403:AD00:1000::/48");
        Parsing.parseInterval("::");
        Parsing.parseInterval("2403:ad00:1000::1");
    }

    @Test
    public void testBadRangeFails() {
        Stream.of("1", "1.2", "1.2.3", "1.2.3.", "255.255.255.-126",
                  "2004:", "2004:2004", "::2004", "2404:8400:ffff::/36",
                  "ff:ff:ff:ff:ff:ff:ff:ff:ff", "ff:ff:ff:ff:ff:ff:ff:ff::")
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
