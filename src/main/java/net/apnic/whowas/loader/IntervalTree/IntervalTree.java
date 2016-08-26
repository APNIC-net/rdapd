package net.apnic.whowas.loader.intervaltree;

import net.apnic.whowas.loader.types.Tuple;

import java.util.Optional;
import java.util.stream.Stream;

public interface IntervalTree<K extends Comparable<K>, V, I extends Interval<K>> {
    Optional<V> exact(I range);
    Stream<Tuple<I,V>> intersecting(I range);
}
