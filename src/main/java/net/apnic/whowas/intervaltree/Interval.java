package net.apnic.whowas.intervaltree;

public interface Interval<K extends Comparable<K>> extends Comparable<Interval<K>> {
    K low();
    K high();

    default int compareTo(Interval<K> other) {
        int lc = low().compareTo(other.low());
        if (lc == 0) {
            lc = high().compareTo(other.high());
        }
        return lc;
    }
}
