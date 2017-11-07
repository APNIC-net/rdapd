package net.apnic.whowas;

import net.apnic.whowas.intervaltree.Interval;
import org.hamcrest.Matcher;

public class FuncTypeSafeMatchers {

    public static <K extends Comparable<K>> Matcher<Interval<K>> encompasses(Interval<K> containee) {
        return new FuncTypeSafeMatcher<>(
                container -> containee.low().compareTo(container.low()) >= 0 && containee.high().compareTo(container.high()) <=0
                , description -> description.appendText(containee + " is contained")
                , (container, description) -> description.appendText(containee + " is not contained by " + container)
        );
    }
}
