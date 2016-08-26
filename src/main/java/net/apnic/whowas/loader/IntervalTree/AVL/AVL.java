package net.apnic.whowas.loader.intervaltree.avl;

import net.apnic.whowas.loader.intervaltree.Interval;
import net.apnic.whowas.loader.intervaltree.IntervalTree;
import net.apnic.whowas.loader.types.Tuple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AVL<K extends Comparable<K>, V, I extends Interval<K>> implements IntervalTree<K, V, I> {
    private volatile Optional<Node> root = Optional.empty();

    public AVL() {
    }

    private Optional<Node> exact(Optional<Node> node, I range) {
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

    public void insert(I range, V value) {
        update(range, value, (a, b) -> {
            throw new IllegalArgumentException("cannot insert duplicate key " + range);
        }, a -> a);
    }

    public <U> void update(I range, U value, BiFunction<V, U, V> append, Function<U, V> create) {
        root = insert(root, range, value, append, create);
    }

    private int heightOf(Optional<Node> n) {
        return n.map(rn -> rn.height).orElse(0);
    }

    private int balanceOf(Node n) {
        return Integer.signum(heightOf(n.left) - heightOf(n.right));
    }

    private Node rotateLeft(Node node) {
        Node right = node.right.orElseThrow(() -> new IllegalArgumentException("Internal AVL tree corruption"));
        Node newLeft = new Node(node.key, node.value, node.left, right.left);
        return new Node(right.key, right.value, Optional.of(newLeft), right.right);
    }

    private Node rotateRight(Node node) {
        Node left = node.left.orElseThrow(() -> new IllegalArgumentException("Internal AVL tree corruption"));
        Node newRight = new Node(node.key, node.value, left.right, node.right);
        return new Node(left.key, left.value, left.left, Optional.of(newRight));
    }

    private <U> Optional<Node> insert(Optional<Node> n, I r, U v, BiFunction<V, U, V> append, Function<U, V> create) {
        return Optional.of(n.map(node -> {
            // BST insert, recursively
            Optional<Node> newLeft = node.left;
            Optional<Node> newRight = node.right;
            switch (Integer.signum(r.compareTo(node.key))) {
                case -1: newLeft = insert(node.left, r, v, append, create); break;
                case  1: newRight = insert(node.right, r, v, append, create); break;
                default: return node.map(e -> append.apply(e, v));    // update!
            }

            node = new Node(node.key, node.value, newLeft, newRight);
            int balance = Integer.signum(heightOf(node.left) - heightOf(node.right));

            switch (balance) {
                case -1:        // left rotate
                    node = new Node(node.key, node.value, node.left,
                        node.right.map(rn -> balanceOf(rn) > 0 ? rotateRight(rn) : rn));
                    node = rotateLeft(node);
                    break;
                case  1:        // right rotate
                    node = new Node(node.key, node.value,
                            node.left.map(ln -> balanceOf(ln) < 0 ? rotateLeft(ln) : ln), node.right);
                    node = rotateRight(node);
            }

            return node;
        }).orElse(new Node(r, create.apply(v))));
    }

    protected class Node {
        final int height;
        final K max;
        final I key;
        final V value;
        final Optional<Node> left;
        final Optional<Node> right;

        protected Node(I key, V value) {
            this.height = 1;
            this.key = key;
            this.max = key.high();
            this.value = value;
            this.left = Optional.empty();
            this.right = Optional.empty();
        }

        protected Node(I key, V value, Optional<Node> left, Optional<Node> right) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.value = value;
            this.height = Integer.max(heightOf(left), heightOf(right)) + 1;
            this.max = Stream.of(Optional.of(key.high()), left.map(n -> n.max), right.map(n -> n.max))
                    .flatMap(o -> o.map(Stream::of).orElse(Stream.empty()))
                    .max(Comparator.naturalOrder()).orElse(key.high());
        }

        protected Node map(Function<V, V> map) {
            return new Node(key, map.apply(value), left, right);
        }

        protected boolean intersects(I range) {
            return range.high().compareTo(key.low()) >= 0 && range.low().compareTo(key.high()) <= 0;
        }

        @Override
        public String toString() {
            return "Node[" + key.low().toString() + " - " + key.high().toString() + "; " + value.toString() + "]";
        }
    }

    private int height(Optional<Node> n) {
        return n.map(rn -> 1 + Integer.max(height(rn.left), height(rn.right))).orElse(0);
    }

    protected int height() {
        return height(root);
    }

    private class IntersectionSpliterator implements Spliterator<Tuple<I, V>> {
        private final Deque<Node> pipe = new LinkedList<>();
        private final I range;

        IntersectionSpliterator(Optional<Node> node, I range) {
            node.ifPresent(pipe::add);
            this.range = range;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Tuple<I, V>> action) {
            while (!pipe.isEmpty()) {
                Node head = pipe.pop();

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
            Node head = pipe.poll();

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

}
