package net.apnic.whowas.intervaltree.avl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.apnic.whowas.intervaltree.Interval;

import java.io.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

class AvlNode<K extends Comparable<K>, V, I extends Interval<K>> implements Serializable {
    final int height;
    final K max;
    final I key;
    final V value;

    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="writeReplace() implemented")
    final Optional<AvlNode<K,V,I>> left;
    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="writeReplace() implemented")
    final Optional<AvlNode<K,V,I>> right;

    AvlNode(I key, V value) {
        this.height = 1;
        this.key = key;
        this.max = key.high();
        this.value = value;
        this.left = Optional.empty();
        this.right = Optional.empty();
    }

    AvlNode(I key, V value, Optional<AvlNode<K,V,I>> left, Optional<AvlNode<K,V,I>> right) {
        this.left = left;
        this.right = right;
        this.key = key;
        this.value = value;
        this.height = Integer.max(
                left.map(n -> n.height).orElse(0),
                right.map(n -> n.height).orElse(0)) + 1;
        this.max = Stream.of(Optional.of(key.high()), left.map(n -> n.max), right.map(n -> n.max))
                .flatMap(o -> o.map(Stream::of).orElse(Stream.empty()))
                .max(Comparator.naturalOrder()).orElse(key.high());
    }

    @SuppressWarnings("unchecked")
    private AvlNode(Wrapper<K,V,I> wrapper) {
        this.height = wrapper.height;
        this.max = wrapper.max;
        this.key = wrapper.key;
        this.value = wrapper.value;
        this.left = Optional.ofNullable(wrapper.left);
        this.right = Optional.ofNullable(wrapper.right);
//        this.left = this.right = Optional.empty();
    }

    AvlNode<K,V,I> map(Function<V, V> map) {
        return new AvlNode<>(key, map.apply(value), left, right);
    }

    boolean intersects(I range) {
        return range.high().compareTo(key.low()) >= 0 && range.low().compareTo(key.high()) <= 0;
    }

    @Override
    public String toString() {
        return "Node[" + key.low().toString() + " - " + key.high().toString() + "; " + value.toString() + "]";
    }

    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper<>(height, max, key, value, left, right);
    }

    private static class Wrapper<K extends Comparable<K>, V, I extends Interval<K>> implements Serializable {
        final int height;
        final K max;
        final I key;
        final V value;
        final AvlNode<K,V,I> left;
        final AvlNode<K,V,I> right;

        public Wrapper(int height, K max, I key, V value, Optional<AvlNode<K, V, I>> left, Optional<AvlNode<K, V, I>> right) {
            this.height = height;
            this.max = max;
            this.key = key;
            this.value = value;
            this.left = left.orElse(null);
            this.right = right.orElse(null);
        }

        private Object readResolve() {
            return new AvlNode<>(this);
        }
    }
}
