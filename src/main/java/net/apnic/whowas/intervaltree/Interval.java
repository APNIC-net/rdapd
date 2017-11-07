package net.apnic.whowas.intervaltree;

public interface Interval<K extends Comparable<K>> extends Comparable<Interval<K>> {
    K low();
    K high();

    default int compareTo(Interval<K> other) {
        // This compare to uses the low values of bother intervals. If one
        // interval is less than the other this dictates order. Should both
        // intervals have the same low value then the high values must be used.
        // Because a lower high value dictactes a lower interval we need to
        // invert the result.
        int lc = low().compareTo(other.low());
        if (lc == 0) {
            lc = -1 * high().compareTo(other.high());
        }
        return lc;
    }
}
