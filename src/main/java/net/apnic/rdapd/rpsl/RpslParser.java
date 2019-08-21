package net.apnic.rdapd.rpsl;

import net.apnic.rdapd.types.Tuple;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
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

    private static final Parser<Void> COMMENT_SYMBOL = Parsers.sequence(Scanners.isChar('#'), WHITESPACE);

    private static final Parser<String> COMMENT = Patterns.many(CharPredicates.notChar('\n'))
            .toScanner("RPSL comment").source()
            .followedBy(Parsers.or(Scanners.isChar('\n'), Parsers.EOF));

    private static final Parser<RpslLineEntry> COMMENT_LINE =
            Parsers.sequence(COMMENT_SYMBOL, COMMENT).map(RpslComment::new);

    private static final Parser<String> LINE = Scanners.many(CharPredicates.and(CharPredicates.notChar('\n'),
                                                             CharPredicates.notChar('#')))
                                                       .source()
                                                       .followedBy(Parsers.or(Scanners.isChar('\n'), Parsers.EOF,
                                                                   COMMENT_LINE));

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

    private static final Parser<RpslLineEntry> ATTRIBUTE_LINE = ATTRIBUTE.map(RpslAttributeEntry::new);

    private static final Parser<RpslLineEntry> RPSL_LINE = Parsers.or(ATTRIBUTE_LINE, COMMENT_LINE);

    private static final Parser<List<RpslLineEntry>> RPSL_ENTRIES = RPSL_LINE.many1();

    private static final Parser<List<Tuple<String, String>>> RPSL_OBJECT = ATTRIBUTE.many1();

    private RpslParser() {
    }

    static List<Tuple<String, String>> parseObject(String input) {
        return RPSL_OBJECT.parse(input);
    }

    static List<Tuple<String, String>> parseObject(byte[] input) {
        return parseObject(new String(input, StandardCharsets.UTF_8));
    }

    /**
     * Notice that only line comments (as opposed to "end of line" comments) are returned by the parser.
     */
    public static List<RpslLineEntry> parseObjectWithComments(String input) {
        return RPSL_ENTRIES.parse(input);
    }

    public static abstract class RpslLineEntry {}

    public static class RpslAttributeEntry extends RpslLineEntry {
        Tuple<String, String> attribute;

        public RpslAttributeEntry(Tuple<String, String> attribute) {
            this.attribute = attribute;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RpslAttributeEntry && this.attribute.equals(((RpslAttributeEntry) obj).attribute);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attribute);
        }
    }

    public static class RpslComment extends RpslLineEntry {
        String comment;

        public RpslComment(String comment) {
            this.comment = comment;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RpslComment && this.comment.equals(((RpslComment) obj).comment);
        }

        @Override
        public int hashCode() {
            return Objects.hash(comment);
        }
    }
}
