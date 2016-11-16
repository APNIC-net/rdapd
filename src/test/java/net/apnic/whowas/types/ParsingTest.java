package net.apnic.whowas.types;

import org.junit.Test;

public class ParsingTest {

    @Test
    public void testParseInterval() throws Exception {
        Parsing.parseInterval("202.12.29.0");
        Parsing.parseInterval("202.12.29.0/24");
        Parsing.parseInterval("2403:AD00:1000::/48");
        Parsing.parseInterval("::");
        Parsing.parseInterval("2403:ad00:1000::1");
    }
}