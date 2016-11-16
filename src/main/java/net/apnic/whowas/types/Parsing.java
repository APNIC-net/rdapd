package net.apnic.whowas.types;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.OptionalInt;

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
            Patterns.many(CharPredicates.among("0123456789abcdefABCDEF:")).toScanner("IPv6 address").source().map(s -> {
                try {
                    return new IP(Inet6Address.getByName(s));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

    private static final Parser<OptionalInt> CIDR =
            Parsers.sequence(
                    Scanners.isChar('/'),
                    Scanners.INTEGER.map(Integer::parseInt).map(OptionalInt::of)
            );

    private static final Parser<IpInterval> IP_RANGE =
            Parsers.sequence(
                    Parsers.longer(V4_ADDRESS, V6_ADDRESS),
                    Parsers.or(CIDR, Parsers.EOF.map(x -> OptionalInt.empty())),
                    (a, c) -> {
                        int maxCidr = a.getAddressFamily() == IP.AddressFamily.IPv4 ? 32 : 128;
                        int cidr = c.orElse(maxCidr);
                        return new IpInterval(a, cidr);
                    }
            );

    // A parser for a Thing;
    // is a function from a String;
    // to a list of pairs of Things and Strings.
    public static IpInterval parseInterval(String thing) {
        return IP_RANGE.parse(thing);
    }
}
