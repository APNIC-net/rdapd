package net.apnic.whowas.types;

import org.junit.Test;

public class ParsingTest {

    @Test
    public void testParseInterval() throws Exception {
        Parsing.parseInterval("202.12.29.0 - 202.12.29.255");
        Parsing.parseInterval("2403:AD00:1000::1/48");

    }
}