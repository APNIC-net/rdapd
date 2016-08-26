package net.apnic.whowas.loader.types;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class IP implements Comparable<IP> {
    private final InetAddress address;

    public IP(InetAddress address) {
        this.address = address;
    }

    @Override
    public int compareTo(IP o) {
        final byte[] l = address.getAddress(), r = o.address.getAddress();
        int cmp = l.length - r.length;

        // Equal address family, compare array contents
        if (cmp == 0) {
            for (int i = 0; i < l.length; i++) {
                int lb = l[i] & 0xff, rb = r[i] & 0xff;
                cmp = lb - rb;
                if (cmp != 0) {
                    break;
                }
            }
        }

        return cmp;
    }

    /**
     * Create a new IP address at the end of the block identified by this IP address plus the given prefix.
     *
     * Will return Optional.empty() if the address and prefix are not appropriately aligned.
     *
     * @param prefix the prefix length to use
     * @return the new IP address, or empty
     */
    public Optional<IP> addPrefix(int prefix) {
        byte[] addr = address.getAddress(); // n.b. assumes this is a clone!

        // The _byte_ offset after which all bits will be one
        int offset = (prefix+7) / 8, bits = 8 - (prefix % 8);

        // set all subsequent bits to 1
        for (int i = offset; i < addr.length; i++) {
            if (addr[i] != 0) { // ... they should be zero now
                return Optional.empty();
            }
            addr[i] = -1;
        }

        // Test and set any remaining bits
        if (bits < 8 && offset > 0) {
            int b = addr[offset - 1] & 0xff;
            int mask = (1 << bits) - 1;
            if ((b & mask) != 0) {
                return Optional.empty();
            }
            b |= mask;
            addr[offset - 1] = (byte)b;
        }
        try {
            return Optional.of(new IP(InetAddress.getByAddress(addr)));
        } catch (UnknownHostException ex) {
            throw new RuntimeException("No seriously, you cannot not know a host I've given you");
        }
    }

    /**
     * The prefix distance from another address, rounding up.
     *
     * If the other address is the wrong address family, this will return Optional.empty().
     *
     * @param other the other address
     * @return the prefix distance
     */
    public Optional<Integer> prefixFrom(IP other) {
        byte[] a1 = this.address.getAddress(),
               a2 = other.address.getAddress();

        if (a1.length != a2.length) {
            return Optional.empty();
        }

        // count number of bits until the addresses differ
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                int h = a1[i] & 0xff, l = a2[i] & 0xff;
                int b = h > l ? h - l : l - h;
                b |= b >> 1;
                b |= b >> 2;
                b |= b >> 4;
                b = 8 - Integer.bitCount(b);
                b += i * 8;
                return Optional.of(b);
            }
        }
        return Optional.of(a1.length * 8);
    }

    @Override
    public boolean equals(Object other) {
        return (other != null && other instanceof IP && address.equals(((IP) other).address));
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return address.getHostAddress();
    }
}
