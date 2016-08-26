package net.apnic.whowas.loader.types;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

public final class Parsing {
    private Parsing() {}

    private static final Parser<IP> V4_ADDRESS =
            Patterns.many(CharPredicates.among("0123456789.")).toScanner("IPv4 address").source().map(s -> {
                try {
                    return new IP(Inet4Address.getByName(s));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });
    private static final Parser<IP> V6_ADDRESS =
            Patterns.many(CharPredicates.among("0123456789abcdef:")).toScanner("IPv6 address").source().map(s -> {
                try {
                    return new IP(Inet6Address.getByName(s));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

    private static final Parser<Void> WHITESPACE = Scanners.WHITESPACES.skipMany();
    private static final Parser<Void> HYPHEN =
            WHITESPACE.followedBy(Scanners.isChar('-')).followedBy(WHITESPACE);

    private static final Parser<IpInterval> IP_RANGE =
            Parsers.or(
                    Parsers.sequence(V4_ADDRESS, HYPHEN, V4_ADDRESS, (a, h, b) -> new IpInterval(a, b)),
                    Parsers.sequence(V4_ADDRESS, Scanners.isChar('/'), Scanners.INTEGER.map(Integer::parseInt),
                            (a, s, b) -> new IpInterval(a, b)),
                    Parsers.sequence(V6_ADDRESS, Scanners.isChar('/'), Scanners.INTEGER.map(Integer::parseInt),
                            (a, s, b) -> new IpInterval(a, b)),
                    V4_ADDRESS.map(a -> new IpInterval(a, 32))
            );

    // A parser for a Thing;
    // is a function from a String;
    // to a list of pairs of Things and Strings.
    // But in this case, there's neither pair nor list, it's just the Thing or another awesome runtime exception.
    public static IpInterval parseInterval(String thing) {
        return IP_RANGE.parse(thing);
    }
}
