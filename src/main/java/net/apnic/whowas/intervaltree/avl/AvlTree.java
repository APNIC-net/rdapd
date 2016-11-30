package net.apnic.whowas.intervaltree.avl;

import net.apnic.whowas.intervaltree.Interval;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.Tuple;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AvlTree<K extends Comparable<K>, V, I extends Interval<K>>
        implements IntervalTree<K, V, I>, Iterable<Tuple<I, V>>, Serializable {
    private static final long serialVersionUID = -6733753719107050254L;

    private final AvlNode<K,V,I> root;

    public AvlTree() {
        root = null;
    }

    private AvlTree(AvlNode<K,V,I> root) {
        this.root = root;
    }

    private Optional<AvlNode<K,V,I>> exact(AvlNode<K, V, I> node, I range) {
        return Optional.ofNullable(node).flatMap(n -> {
            switch (Integer.signum(range.compareTo(n.key))) {
                case -1: return exact(n.left, range);
                case  1: return exact(n.right, range);
            }
            return Optional.of(n);
        });
    }

    @Override
    public Optional<V> exact(I range) {
        return exact(root, range).map(n -> n.value);
    }

    @Override
    public Stream<Tuple<I, V>> intersecting(I range) {
        return StreamSupport.stream(new IntersectionSpliterator(root, range), true);
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(AvlNode<K, V, I> node) {
        return node == null ? 0 : 1 + size(node.left) + size(node.right);
    }

    @Override
    public Iterator<Tuple<I, V>> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator<Tuple<I, V>> spliterator() {
        return new AvlSpliterator(root);
    }

    AvlTree<K, V, I> insert(I range, V value) {
        return update(range, value, (a, b) -> {
            throw new IllegalArgumentException("cannot insert duplicate key " + range);
        }, a -> a);
    }

    public <U> AvlTree<K, V, I> update(I range, U value, BiFunction<V, U, V> append, Function<U, V> create) {
        return new AvlTree<>(insert(root, range, value, append, create));
    }

    private int heightOf(AvlNode<K, V, I> n) {
        return n == null ? 0 : n.height;
    }

    private int balanceOf(AvlNode<K,V,I> n) {
        return Integer.signum(heightOf(n.left) - heightOf(n.right));
    }

    private AvlNode<K,V,I> rotateLeft(AvlNode<K,V,I> node) {
        AvlNode<K,V,I> right = node.right;
        AvlNode<K,V,I> newLeft = new AvlNode<>(node.key, node.value, node.left, right.left);
        return new AvlNode<>(right.key, right.value, newLeft, right.right);
    }

    private AvlNode<K,V,I> rotateRight(AvlNode<K,V,I> node) {
        AvlNode<K,V,I> left = node.left;
        AvlNode<K,V,I> newRight = new AvlNode<>(node.key, node.value, left.right, node.right);
        return new AvlNode<>(left.key, left.value, left.left, newRight);
    }

    private <U> AvlNode<K,V,I> insert(AvlNode<K,V,I> n, I r, U v, BiFunction<V, U, V> append, Function<U, V> create) {
        return Optional.ofNullable(n).map(node -> {
            // BST insert, recursively
            AvlNode<K,V,I> newLeft = node.left;
            AvlNode<K,V,I> newRight = node.right;
            switch (Integer.signum(r.compareTo(node.key))) {
                case -1: newLeft = insert(node.left, r, v, append, create); break;
                case  1: newRight = insert(node.right, r, v, append, create); break;
                default: return node.map(e -> append.apply(e, v));    // update!
            }

            node = new AvlNode<>(node.key, node.value, newLeft, newRight);
            int balance = Integer.signum(heightOf(node.left) - heightOf(node.right));

            switch (balance) {
                case -1:        // left rotate
                    node = new AvlNode<>(node.key, node.value, node.left,
                        node.right == null || balanceOf(node.right) <= 0 ? node.right : rotateRight(node.right));
                    node = rotateLeft(node);
                    break;
                case  1:        // right rotate
                    node = new AvlNode<>(node.key, node.value,
                            node.left == null || balanceOf(node.left) >= 0 ? node.left : rotateLeft(node.left),
                            node.right);
                    node = rotateRight(node);
            }

            return node;
        }).orElse(new AvlNode<>(r, create.apply(v)));
    }

    AvlNode<K, V, I> getRoot() {
        return root;
    }

    private class AvlSpliterator implements Spliterator<Tuple<I, V>> {
        private final Deque<AvlNode<K,V,I>> pipe = new LinkedList<>();

        AvlSpliterator(AvlNode<K,V,I> root) {
            add(root);
        }

        private void add(AvlNode<K, V, I> node) {
            if (node != null) {
                pipe.add(node);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Tuple<I, V>> action) {
            if (!pipe.isEmpty()) {
                AvlNode<K,V,I> head = pipe.pop();

                add(head.left);
                add(head.right);
                action.accept(new Tuple<>(head.key, head.value));
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<Tuple<I, V>> trySplit() {
            AvlNode<K,V,I> head = pipe.poll();

            if (head != null) {
                if (pipe.isEmpty() || head.height < 4) {
                    pipe.push(head);
                    return null;
                } else {
                    return new AvlSpliterator(head);
                }
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.IMMUTABLE;
        }
    }

    public interface NodePredicate<K extends Comparable<K>, V, I extends Interval<K>>
            extends Predicate<AvlNode<K, V, I>> {

        boolean testNode(K max, I key, V value);

        default boolean test(AvlNode<K, V, I> node) {
            return testNode(node.max, node.key, node.value);
        }
    }

    private class IntersectionSpliterator implements Spliterator<Tuple<I, V>> {
        private final Deque<AvlNode<K,V,I>> pipe = new LinkedList<>();
        private final I range;

        IntersectionSpliterator(AvlNode<K,V,I> node, I range) {
            add(node);
            this.range = range;
        }

        private void add(AvlNode<K, V, I> node) {
            if (node != null) {
                pipe.add(node);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Tuple<I, V>> action) {
            while (!pipe.isEmpty()) {
                AvlNode<K,V,I> head = pipe.pop();

                // Push the kids onto the pipe
                // Always skip a sub-tree whose max is lower than the start of the range
                Optional.ofNullable(head.left).filter(n -> n.max.compareTo(range.low()) >= 0).ifPresent(pipe::add);
                // If the low value of this node is greater than the end of the range, skip the right sub-tree
                if (head.key.low().compareTo(range.high()) <= 0) {
                    Optional.ofNullable(head.right).filter(n -> n.max.compareTo(range.low()) >= 0).ifPresent(pipe::add);
                }

                // This node is relevant iff it intersects the range
                if (head.intersects(range)) {
                    action.accept(new Tuple<>(head.key, head.value));
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<Tuple<I, V>> trySplit() {
            AvlNode<K,V,I> head = pipe.poll();

            if (head != null) {
                if (pipe.isEmpty() || head.height < 4) {
                    pipe.push(head);
                    return null;
                } else {
                    return new IntersectionSpliterator(head, range);
                }
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.IMMUTABLE;
        }
    }

    /* Serialization via a replacement to get around immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper<>(root);
    }

    private static class Wrapper<K extends Comparable<K>, V, I extends Interval<K>> implements Serializable {
        final AvlNode<K,V,I> root;

        Wrapper(AvlNode<K, V, I> root) {
            this.root = root;
        }

        private Object readResolve() {
            return new AvlTree<>(root);
        }
    }
}
