package net.apnic.whowas.types;

import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IPTest {
    @Test
    public void testOrdering() throws Exception {
        List<IP> IPs = Arrays.asList(
                new IP(Inet4Address.getByName("4.3.2.1")),
                new IP(Inet4Address.getByName("1.2.3.4")),
                new IP(Inet4Address.getByName("202.12.29.219"))
        );
        IPs.sort(null);
        assertThat("the smallest IP is 1.2.3.4", IPs.get(0), is(equalTo(new IP(Inet4Address.getByName("1.2.3.4")))));
    }

    private IP toIP(String string) {
        try {
            return new IP(InetAddress.getByName(string));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPrefixCount() throws Exception {
        IP base = new IP(Inet4Address.getByName("64.0.0.0"));
        List<Tuple<String, Integer>> tests = Arrays.asList(
                new Tuple<>("127.255.255.255", 2),
                new Tuple<>("72.19.44.37", 4),
                new Tuple<>("64.0.0.255", 24)
        );
        tests.forEach(v -> {
            assertThat("Test address " + v.first() + " is correct", base.prefixFrom(toIP(v.first())), is(equalTo(Optional.of(v.second()))));
        });


    }

}
