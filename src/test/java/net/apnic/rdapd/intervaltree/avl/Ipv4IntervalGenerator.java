package net.apnic.rdapd.intervaltree.avl;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import net.apnic.rdapd.types.IP;
import net.apnic.rdapd.types.IpInterval;
import net.apnic.rdapd.types.Parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ipv4IntervalGenerator extends Generator<IpInterval> {

    int lowerBound = low(Parsing.parseCIDRInterval("0.0.0.0"));
    int upperBound = low(Parsing.parseCIDRInterval("255.255.255.255"));
    IpInterval encompassed = Parsing.parseCIDRInterval("10.0.127.0/24");

    public Ipv4IntervalGenerator() {
        super(IpInterval.class);
    }

    @Override
    public IpInterval generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {

        //to encompass encompassed, this must be equal to or lower than the lowest address of the encompassed range
        int generatedAddress = sourceOfRandomness.nextInt(lowerBound, low(encompassed));


        List<Integer> prefixes = IntStream.range(0, 32)
                .filter(prefix -> {
                    int netmask = networkMask(prefix);
                    int addressMask = ~netmask;

                    int minRange = high(this.encompassed) - (generatedAddress & netmask);
                    int maxRange = upperBound - (generatedAddress & netmask);

                    return addressMask >= minRange && addressMask <= maxRange && (generatedAddress & netmask) >= lowerBound;
                }).boxed().collect(Collectors.toList());

        int prefix = sourceOfRandomness.choose(prefixes);

        try {
            return new IpInterval(
                    new IP(Inet4Address.getByAddress(intToByteArray(generatedAddress & networkMask(prefix))))
                    , prefix
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void configure(InRange range) {
        lowerBound = low(Parsing.parseCIDRInterval(range.min()));
        upperBound = high(Parsing.parseCIDRInterval(range.max()));
    }

    public void configure(Encompassed encompassed) {
        this.encompassed = Parsing.parseCIDRInterval(encompassed.value());
    }

    private static int byteArrayToInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        return bb.getInt();
    }

    private static byte[] intToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.putInt(i);
        return bb.array();
    }

    private static int networkMask(int prefix) {
        return 0xFFFFFFFF << 32 - prefix;
    }

    private static int high(IpInterval ipInterval) {
        return byteArrayToInt(ipInterval.high().getAddress().getAddress());
    }

    private static int low(IpInterval ipInterval) {
        return byteArrayToInt(ipInterval.low().getAddress().getAddress());
    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @GeneratorConfiguration
    public @interface Encompassed {
        String value();
    }
}