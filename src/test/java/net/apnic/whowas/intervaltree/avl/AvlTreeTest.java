package net.apnic.whowas.intervaltree.avl;

import net.apnic.whowas.types.Tuple;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AvlTreeTest {
    private int height(AvlNode<?, ?, ?> node) {
        return node == null ? 0 : 1 + Math.max(height(node.left), height(node.right));
    }

    @Test
    public void testInsert() throws Exception {
        AvlTree<Integer, String, IntInterval> tree = new AvlTree<>();

        tree = tree.insert(new IntInterval(42, 59), "green tree");
        tree = tree.insert(new IntInterval(33, 45), "corroboree");
        tree = tree.insert(new IntInterval(39, 39), "bleating tree");
        assertThat("Three nodes has a max height of 2", height(tree.getRoot()), is(2));
    }

    @Test
    public void testExact() throws Exception {
        AvlTree<Integer, String, IntInterval> tree = new AvlTree<>();
        tree = tree.insert(new IntInterval(42, 59), "green tree");
        tree = tree.insert(new IntInterval(33, 45), "corroboree");
        tree = tree.insert(new IntInterval(33, 44), "rocket");
        tree = tree.insert(new IntInterval(39, 39), "bleating tree");
        tree = tree.insert(new IntInterval(20, 39), "tusked");
        assertThat("finding a missing range gives nothing", tree.exact(new IntInterval(33, 46)), is(Optional.empty()));
        assertThat("finding a precise range gives its value", tree.exact(new IntInterval(33,45)), is(Optional.of("corroboree")));
        assertThat("equal starts don't matter", tree.exact(new IntInterval(33, 44)), is(Optional.of("rocket")));
        assertThat("equal ends don't matter", tree.exact(new IntInterval(20, 39)), is(Optional.of("tusked")));
    }

    @Test
    public void testEqualToAndLeastSpecific() throws Exception
    {
        AvlTree<Integer, String, IntInterval> tree = new AvlTree<>();
        tree = tree.insert(new IntInterval(20, 30), "green tree");
        tree = tree.insert(new IntInterval(33, 45), "corroboree");
        tree = tree.insert(new IntInterval(30, 42), "rocket");
        tree = tree.insert(new IntInterval(39, 39), "bleating tree");
        tree = tree.insert(new IntInterval(20, 39), "tusked");
        tree = tree.insert(new IntInterval(20, 38), "space");
        tree = tree.insert(new IntInterval(40, 50), "blue");
        List<String> frogs = tree.equalToAndLeastSpecific(new IntInterval(38, 40))
            .map(Tuple::snd).collect(Collectors.toList());
        assertThat("there are two frogs in range", frogs.size(), is(2));
        assertThat("there exists a corroboree", frogs.contains("corroboree"), is(true));
        assertThat("there exists a rocket", frogs.contains("rocket"), is(true));

        frogs = tree.equalToAndLeastSpecific(new IntInterval(0, 100))
            .map(Tuple::snd).collect(Collectors.toList());
        assertThat("there are no frogs in range", frogs.size(), is(0));

        frogs = tree.equalToAndLeastSpecific(new IntInterval(41, 49))
            .map(Tuple::snd).collect(Collectors.toList());
        assertThat("there is one frog in range", frogs.size(), is(1));
        assertThat("there exists a blue", frogs.contains("blue"), is(true));

        frogs = tree.equalToAndLeastSpecific(new IntInterval(40, 50))
            .map(Tuple::snd).collect(Collectors.toList());
        assertThat("there are is one frog in range", frogs.size(), is(1));
        assertThat("there exists a blue", frogs.contains("blue"), is(true));
    }

    @Test
    public void rangeTest() throws Exception {
        AvlTree<Integer, String, IntInterval> tree = new AvlTree<>();
        tree = tree.insert(new IntInterval(42, 59), "green tree");
        tree = tree.insert(new IntInterval(33, 45), "corroboree");
        tree = tree.insert(new IntInterval(33, 44), "rocket");
        tree = tree.insert(new IntInterval(39, 39), "bleating tree");
        tree = tree.insert(new IntInterval(20, 39), "tusked");
        List<String> frogs = tree.intersecting(new IntInterval(40, 50)).map(Tuple::snd).collect(Collectors.toList());
        assertThat("there are three frogs in range", frogs.size(), is(3));
        assertThat("there are no frogs in negative-land", tree.intersecting(new IntInterval(-1, -1)).count(), is(0L));
    }

    @Test
    public void concurrencyTest() throws Exception {
        AvlTree<Integer, Integer, IntInterval> tree = new AvlTree<>();
        Random random = new Random();
        int[] expected = { 0 };
        for (int i = 0; i < 10000; i++) {
            int a = random.nextInt(), b = random.nextInt();
            if (a > b) {
                int c = a;
                a = b;
                b = c;
            }
            IntInterval iv = new IntInterval(a, b);
            if (iv.low() <= 1000 && iv.high() >= -1000) {
                expected[0]++;
            }
            tree = tree.update(iv, random.nextInt(), (p, q) -> {
                if (iv.low() <= 1000 && iv.high() >= -1000) {
                    expected[0]--;
                }
                return p+q;
            }, z -> iv.low());
        }

        IntInterval nearZero = new IntInterval(-1000, 1000);
        assertTrue("the intersecting stream is parallel", tree.intersecting(nearZero).isParallel());
        List<Integer> theNumbers = tree.intersecting(nearZero).map(Tuple::snd).collect(Collectors.toList());
        assertThat("There's the expected number of, er, numbers", theNumbers.size(), is(expected[0]));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        AvlTree<Integer, LocalDateTime, IntInterval> tree = new AvlTree<>();
        Random random = new Random();
        Stream.generate(() -> new IntInterval(random.nextInt(), random.nextInt()))
                .limit(10000)
                .map(i -> i.high() >= i.low() ? i : new IntInterval(i.high(), i.low()))
                .forEach(i -> tree.update(i, random.nextInt(),
                        (p, q) -> LocalDateTime.ofEpochSecond(random.nextInt(999999999), 0, ZoneOffset.UTC),
                        j -> LocalDateTime.ofEpochSecond(random.nextInt(999999999), 0, ZoneOffset.UTC)));


        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        byte data[] = conf.asByteArray(tree);
        AvlTree<Integer, LocalDateTime, IntInterval> tree2 = (AvlTree)conf.asObject(data);
        assertThat("Serialise/deserialise is precise",
                tree.iterator(), is(iteratorMatcher(tree2.iterator())));

    }

    private <T> Matcher<Iterator<T>> iteratorMatcher(Iterator<T> it) {
        return new TypeSafeDiagnosingMatcher<Iterator<T>>() {
            @Override
            protected boolean matchesSafely(Iterator<T> item, Description mismatchDescription) {
                int i = 0;
                while (it.hasNext() && item.hasNext()) {
                    Matcher<T> sub = equalTo(item.next());
                    T nxt = it.next();
                    if (!sub.matches(nxt)) {
                        mismatchDescription.appendText("Mismatch in iterator at index " + i + ": ");
                        sub.describeMismatch(nxt, mismatchDescription);
                        mismatchDescription.appendText(", should be ");
                        sub.describeTo(mismatchDescription);
                        return false;
                    }
                    i++;
                }
                if (it.hasNext() != item.hasNext()) {
                    mismatchDescription.appendText("Iterators are unequal lengths at index " + i);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Iterators should match precisely");
            }
        };
    }
}
