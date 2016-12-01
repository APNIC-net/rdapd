package net.apnic.whowas.rpsl;

import net.apnic.whowas.types.Tuple;
import org.codehaus.jparsec.error.ParserException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RpslParserTest {
    @Test
    public void parseNothingMuch() {
        String input = "person:    Example Citizen\n";

        List<Tuple<String, String>> output = RpslParser.parseObject(input);

        assertThat("Parsed a simple one-attribute object", output,
                is(Collections.singletonList(new Tuple<>("person", "Example Citizen"))));
    }

    @Test
    public void parseTwoAttributes() {
        String input = "person:  Example Citizen\nhandle:EC44-AP\n";
        List<Tuple<String, String>> expected = Arrays.asList(
                new Tuple<>("person", "Example Citizen"),
                new Tuple<>("handle", "EC44-AP"));

        assertThat("Two attributes looks good", RpslParser.parseObject(input),
                is(expected));
    }

    @Test
    public void parseThreeAttributes() {
        String input = "person:  Example Citizen\nhandle:EC44-AP\nsource:\t\tTEST\n";
        List<Tuple<String, String>> expected = Arrays.asList(
                new Tuple<>("person", "Example Citizen"),
                new Tuple<>("handle", "EC44-AP"),
                new Tuple<>("source", "TEST"));

        assertThat("Three attributes as expected", RpslParser.parseObject(input),
                is(expected));
    }

    @Test(expected = ParserException.class)
    public void newlineRequired() {
        RpslParser.parseObject("role: Tester\nhandle:TST1-AP");
    }

    @Test
    public void whitespaceContinuations() {
        String input = "person:  Example Citizen\nremarks: a continuation\n     line.\nsource:\t\tTEST\n";
        List<Tuple<String, String>> expected = Arrays.asList(
                new Tuple<>("person", "Example Citizen"),
                new Tuple<>("remarks", "a continuation line."),
                new Tuple<>("source", "TEST"));

        assertThat("Line continued correctly", RpslParser.parseObject(input),
                is(expected));
    }

    @Test
    public void plusContinuations() {
        String input = "person:  Example Citizen\nremarks: a continuation\n+\n+     line.\nsource:\t\tTEST\n";
        List<Tuple<String, String>> expected = Arrays.asList(
                new Tuple<>("person", "Example Citizen"),
                new Tuple<>("remarks", "a continuation       line."),
                new Tuple<>("source", "TEST"));

        assertThat("Line continued correctly", RpslParser.parseObject(input),
                is(expected));
    }

    @Test
    public void extendedCharacters() {
        String input = "person:  Éxample çitizen\nhandle:EC44-AP\nsource:\t\tαβγδε\n";
        List<Tuple<String, String>> expected = Arrays.asList(
                new Tuple<>("person", "Éxample çitizen"),
                new Tuple<>("handle", "EC44-AP"),
                new Tuple<>("source", "αβγδε"));

        assertThat("Three attributes as expected", RpslParser.parseObject(input),
                is(expected));
    }
}