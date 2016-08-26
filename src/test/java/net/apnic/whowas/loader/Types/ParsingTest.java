package net.apnic.whowas.loader.types;

import org.junit.Test;

public class ParsingTest {

    @Test
    public void testParseInterval() throws Exception {
        IpInterval interval = Parsing.parseInterval("202.12.29.0 - 202.12.29.255");
    }
}