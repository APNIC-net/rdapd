package net.apnic.whowas.intervaltree;

import net.apnic.whowas.types.Tuple;

import java.util.Optional;
import java.util.stream.Stream;

public interface IntervalTree<K extends Comparable<K>, V, I extends Interval<K>> {
    Stream<Tuple<I, V>> equalToAndLeastSpecific(I range);
    Optional<V> exact(I range);
    Stream<Tuple<I,V>> intersecting(I range);
    int size();
}
