package net.apnic.whowas.intervaltree.avl;

import net.apnic.whowas.intervaltree.Interval;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AvlTree<K extends Comparable<K>, V, I extends Interval<K>>
        implements IntervalTree<K, V, I>, Iterable<Tuple<I, V>>, Serializable {
    private volatile Optional<AvlNode<K,V,I>> root = Optional.empty();

    public AvlTree() {
    }

    private Optional<AvlNode<K,V,I>> exact(Optional<AvlNode<K, V, I>> node, I range) {
        return node.flatMap(n -> {
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
    public Iterator<Tuple<I, V>> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator<Tuple<I, V>> spliterator() {
        return new AvlSpliterator(root);
    }

    public void insert(I range, V value) {
        update(range, value, (a, b) -> {
            throw new IllegalArgumentException("cannot insert duplicate key " + range);
        }, a -> a);
    }

    public <U> void update(I range, U value, BiFunction<V, U, V> append, Function<U, V> create) {
        root = insert(root, range, value, append, create);
    }

    private int heightOf(Optional<AvlNode<K,V,I>> n) {
        return n.map(z -> z.height).orElse(0);
    }

    private int balanceOf(AvlNode<K,V,I> n) {
        return Integer.signum(heightOf(n.left) - heightOf(n.right));
    }

    private AvlNode<K,V,I> rotateLeft(AvlNode<K,V,I> node) {
        AvlNode<K,V,I> right = node.right.orElseThrow(() -> new IllegalArgumentException("Internal AVL tree corruption"));
        AvlNode<K,V,I> newLeft = new AvlNode<>(node.key, node.value, node.left, right.left);
        return new AvlNode<>(right.key, right.value, Optional.of(newLeft), right.right);
    }

    private AvlNode<K,V,I> rotateRight(AvlNode<K,V,I> node) {
        AvlNode<K,V,I> left = node.left.orElseThrow(() -> new IllegalArgumentException("Internal AVL tree corruption"));
        AvlNode<K,V,I> newRight = new AvlNode<>(node.key, node.value, left.right, node.right);
        return new AvlNode<>(left.key, left.value, left.left, Optional.of(newRight));
    }

    private <U> Optional<AvlNode<K,V,I>> insert(Optional<AvlNode<K,V,I>> n, I r, U v, BiFunction<V, U, V> append, Function<U, V> create) {
        return Optional.of(n.map(node -> {
            // BST insert, recursively
            Optional<AvlNode<K,V,I>> newLeft = node.left;
            Optional<AvlNode<K,V,I>> newRight = node.right;
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
                        node.right.map(rn -> balanceOf(rn) > 0 ? rotateRight(rn) : rn));
                    node = rotateLeft(node);
                    break;
                case  1:        // right rotate
                    node = new AvlNode<>(node.key, node.value,
                            node.left.map(ln -> balanceOf(ln) < 0 ? rotateLeft(ln) : ln), node.right);
                    node = rotateRight(node);
            }

            return node;
        }).orElse(new AvlNode<>(r, create.apply(v))));
    }

    private int height(Optional<AvlNode<K,V,I>> n) {
        return n.map(rn -> 1 + Integer.max(height(rn.left), height(rn.right))).orElse(0);
    }

    protected int height() {
        return height(root);
    }

    private class AvlSpliterator implements Spliterator<Tuple<I, V>> {
        private final Deque<AvlNode<K,V,I>> pipe = new LinkedList<>();

        AvlSpliterator(Optional<AvlNode<K,V,I>> root) {
            root.ifPresent(pipe::add);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Tuple<I, V>> action) {
            if (!pipe.isEmpty()) {
                AvlNode<K,V,I> head = pipe.pop();

                head.left.ifPresent(pipe::add);
                head.right.ifPresent(pipe::add);
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
                    return new AvlSpliterator(Optional.of(head));
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

        IntersectionSpliterator(Optional<AvlNode<K,V,I>> node, I range) {
            node.ifPresent(pipe::add);
            this.range = range;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Tuple<I, V>> action) {
            while (!pipe.isEmpty()) {
                AvlNode<K,V,I> head = pipe.pop();

                // Push the kids onto the pipe
                // Always skip a sub-tree whose max is lower than the start of the range
                head.left.filter(n -> n.max.compareTo(range.low()) >= 0).ifPresent(pipe::add);
                // If the low value of this node is greater than the end of the range, skip the right sub-tree
                if (head.key.low().compareTo(range.high()) <= 0) {
                    head.right.filter(n -> n.max.compareTo(range.low()) >= 0).ifPresent(pipe::add);
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
                    return new IntersectionSpliterator(Optional.of(head), range);
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

    /* Serialization requires special methods due to the heavy use of Optional<> */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(root.orElse(null));
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        root = Optional.of((AvlNode<K,V,I>)in.readObject());
    }
}
