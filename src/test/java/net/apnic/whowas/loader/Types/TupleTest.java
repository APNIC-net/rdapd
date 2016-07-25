package net.apnic.whowas.loader.Types;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTest {
    private class Banana {
        Banana() {}
    }

    @Test
    public void testEquals() throws Exception {
        //noinspection ObjectEqualsNull
        assertFalse("A Tuple is never equal to null", new Tuple<>("a", "b").equals(null));
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse("A Tuple is never equal to a banana", new Tuple<>("a", "b").equals(new Banana()));
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse("Two Tuples are not equal if types do not match", new Tuple<>("a", "b").equals(new Tuple<>(new Banana(), "b")));
        assertFalse("Two Tuples are not equal if values do not match", new Tuple<>("a", "b").equals(new Tuple<>("b", "a")));

        // We assume that String stands in for all possible types, why not.
        assertTrue("Two Tuples are equal if values match", new Tuple<>("a", "b").equals(new Tuple<>("a", "b")));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals("Two Tuples with the same values have the same hashCode", new Tuple<>("a", "b").hashCode(), new Tuple<>("a", "b").hashCode());
        assertNotEquals("Two Tuples with different values _probably_ have different hashCodes", new Tuple<>("b", "a").hashCode(), new Tuple<>("a", "b").hashCode());
    }
}