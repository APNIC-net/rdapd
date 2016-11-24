package net.apnic.whowas.rpsl;

import com.google.common.base.Charsets;
import net.apnic.whowas.types.Tuple;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An RPSL parser for the modern software system.  Probably not blisteringly
 * fast.
 */
public class RpslParser {
    private static final Parser<Void> WHITESPACE = Scanners.many(CharPredicates.among(" \t"));

    private static final Parser<String> ATTRIBUTE_NAME
            = Patterns.many1(CharPredicates.or(CharPredicates.IS_ALPHA_NUMERIC, CharPredicates.isChar('-')))
            .toScanner("RPSL attribute name").source().map(String::toLowerCase);

    private static final Parser<String> LINE = Scanners.many(CharPredicates.notChar('\n')).source()
            .followedBy(Scanners.isChar('\n'));

    private static final Parser<Void> CONTINUATION_MARKER = Parsers.or(
            Scanners.among(" \t").followedBy(WHITESPACE),
            Scanners.isChar('+'));

    private static final Parser<String> ATTRIBUTE_VALUE = LINE.sepBy1(CONTINUATION_MARKER)
            .map(l -> l.stream().collect(Collectors.joining(" ")));

    private static final Parser<Void> COLON = Parsers.sequence(
            WHITESPACE, Scanners.isChar(':'), WHITESPACE);

    private static final Parser<Tuple<String, String>> ATTRIBUTE = Parsers.sequence(
            ATTRIBUTE_NAME,
            COLON,
            ATTRIBUTE_VALUE,
            (n, w, v) -> new Tuple<>(n, v));

    private static final Parser<List<Tuple<String, String>>> RPSL_OBJECT = ATTRIBUTE.many1();

    private RpslParser() {
    }

    public static List<Tuple<String, String>> parseObject(String input) {
        return RPSL_OBJECT.parse(input);
    }

    public static List<Tuple<String, String>> parseObject(byte[] input) {
        return RPSL_OBJECT.parse(new String(input, Charsets.UTF_8));
    }
}
