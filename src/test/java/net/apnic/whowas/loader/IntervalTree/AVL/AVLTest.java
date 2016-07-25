package net.apnic.whowas.loader.IntervalTree.AVL;

import net.apnic.whowas.loader.IntervalTree.Interval;
import net.apnic.whowas.loader.Types.Tuple;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class AVLTest {
    private class IntInterval implements Interval<Integer> {
        private final int l, h;
        private IntInterval(int l, int h) {
            this.l = l;
            this.h = h;
        }

        @Override
        public Integer low() {
            return l;
        }

        @Override
        public Integer high() {
            return h;
        }

        @Override
        public String toString() {
            return "(" + l + " - " + h + ")";
        }
    }

    @Test
    public void testInsert() throws Exception {
        AVL<Integer, String, IntInterval> tree = new AVL<>();

        tree.insert(new IntInterval(42, 59), "green tree");
        tree.insert(new IntInterval(33, 45), "corroboree");
        tree.insert(new IntInterval(39, 39), "bleating tree");
        assertThat("Three nodes has a max height of 2", tree.height(), is(2));
    }

    @Test
    public void testExact() throws Exception {
        AVL<Integer, String, IntInterval> tree = new AVL<>();
        tree.insert(new IntInterval(42, 59), "green tree");
        tree.insert(new IntInterval(33, 45), "corroboree");
        tree.insert(new IntInterval(33, 44), "rocket");
        tree.insert(new IntInterval(39, 39), "bleating tree");
        tree.insert(new IntInterval(20, 39), "tusked");
        assertThat("finding a missing range gives nothing", tree.exact(new IntInterval(33, 46)), is(Optional.empty()));
        assertThat("finding a precise range gives its value", tree.exact(new IntInterval(33,45)), is(Optional.of("corroboree")));
        assertThat("equal starts don't matter", tree.exact(new IntInterval(33, 44)), is(Optional.of("rocket")));
        assertThat("equal ends don't matter", tree.exact(new IntInterval(20, 39)), is(Optional.of("tusked")));
    }

    @Test
    public void rangeTest() throws Exception {
        AVL<Integer, String, IntInterval> tree = new AVL<>();
        tree.insert(new IntInterval(42, 59), "green tree");
        tree.insert(new IntInterval(33, 45), "corroboree");
        tree.insert(new IntInterval(33, 44), "rocket");
        tree.insert(new IntInterval(39, 39), "bleating tree");
        tree.insert(new IntInterval(20, 39), "tusked");
        List<String> frogs = tree.intersecting(new IntInterval(40, 50)).map(Tuple::snd).collect(Collectors.toList());
        assertThat("there are three frogs in range", frogs.size(), is(3));
        assertThat("there are no frogs in negative-land", tree.intersecting(new IntInterval(-1, -1)).count(), is(0L));
    }

    @Test
    public void concurrencyTest() throws Exception {
        AVL<Integer, Integer, IntInterval> tree = new AVL<>();
        Random random = new Random();
        final int[] expected = { 0 };
        Stream.generate(() -> new IntInterval(random.nextInt(), random.nextInt()))
                .limit(10000)
                .map(i -> i.high() >= i.low() ? i : new IntInterval(i.high(), i.low()))
                .forEach(i -> {
                    if (i.low() <= 1000 && i.high() >= -1000) {
                        expected[0]++;
                    }
                    tree.update(i, random.nextInt(), (p, q) -> {
                        if (i.low() <= 1000 && i.high() >= -1000) {
                            expected[0]--;
                        }
                        return p + q;
                    }, j -> i.low());
                });

        IntInterval nearZero = new IntInterval(-1000, 1000);
        assertTrue("the intersecting stream is parallel", tree.intersecting(nearZero).isParallel());
        List<Integer> theNumbers = tree.intersecting(nearZero).map(Tuple::snd).collect(Collectors.toList());
        assertThat("There's the expected number of, er, numbers", theNumbers.size(), is(expected[0]));
    }
}