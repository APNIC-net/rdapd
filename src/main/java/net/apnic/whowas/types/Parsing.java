package net.apnic.whowas.types;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.OptionalInt;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.pattern.CharPredicate;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.pattern.Patterns;
import org.codehaus.jparsec.Scanners;

public final class Parsing {
    private static final CharPredicate IPV6_SEC_PREDICATE =
        CharPredicates.among("0123456789abcdefABCDEF");

    private static final Pattern IPV6_SEC_PATTERN =
        Patterns.times(1, 4, IPV6_SEC_PREDICATE);

    private static final Pattern IPV6_SEC_COLON_PATTERN =
        IPV6_SEC_PATTERN.next(Patterns.isChar(':'));

    private static final Pattern IPV6_DCOLON_END_PATTERN =
        IPV6_SEC_COLON_PATTERN.times(0, 5).next(IPV6_SEC_PATTERN)
            .or(Patterns.times(0, 4, IPV6_SEC_PREDICATE));

//    private static final Pattern IPV6_ALL_PATTERN =
//        Patterns.isChar(':').next(Patterns.isChar(':'));

    private static final Pattern IPV6_ALL_SEC_PATTERN =
        IPV6_SEC_COLON_PATTERN.times(7).next(IPV6_SEC_PATTERN);

    private static final Pattern IPV6_PART_SEC_PATTERN =
        IPV6_SEC_COLON_PATTERN.times(1, 6).or(Patterns.isChar(':'))
            .next(Patterns.isChar(':'))
            .next(IPV6_DCOLON_END_PATTERN);

    private static final Pattern IPV6_FULL_PATTERN =
        IPV6_PART_SEC_PATTERN.or(IPV6_ALL_SEC_PATTERN);

    private Parsing() {}

    private static final Parser<IP> V4_ADDRESS =
        Patterns.INTEGER
            .next(Patterns.isChar('.'))
            .times(3)
            .next(Patterns.INTEGER).toScanner("IPv44 Address").source()
            .map(s ->
            {
                try {
                    return new IP(Inet4Address.getByName(s));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

    private static final Parser<IP> V6_ADDRESS =
        IPV6_FULL_PATTERN
            .toScanner("IPv6 address").source().map(s -> {
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

    private static final Parser<Void> HYPHEN =
            Parsers.sequence(
                    Scanners.WHITESPACES.optional(),
                    Scanners.isChar('-'),
                    Scanners.WHITESPACES.optional()
            );

    private static final Parser<IpInterval> IP4_START_END =
            Parsers.sequence(
                    V4_ADDRESS,
                    HYPHEN,
                    V4_ADDRESS,
                    (f, h, t) -> new IpInterval(f, t)
            );

    private static final Parser<IpInterval> IP_CIDR =
            Parsers.sequence(
                    Parsers.longer(V4_ADDRESS, V6_ADDRESS),
                    Parsers.or(CIDR, Parsers.EOF.map(x -> OptionalInt.empty())),
                    (a, c) -> {
                        int maxCidr = a.getAddressFamily() == IP.AddressFamily.IPv4 ? 32 : 128;
                        int cidr = c.orElse(maxCidr);
                        return new IpInterval(a, cidr);
                    }
            );

    private static final Parser<IpInterval> IP_RANGE =
            Parsers.longer(IP_CIDR, IP4_START_END);

    public static IpInterval parseCIDRInterval(String thing) {
        System.out.println(thing);
        try {
            return IP_CIDR.parse(thing);
        } catch(Exception e) {
            System.out.println(e);
            throw new RuntimeException("Could not parse address " + thing, e);
        }
    }

    public static IpInterval parseInterval(String thing) {
        try {
            return IP_RANGE.parse(thing);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse address " + thing, e);
        }
    }
}
