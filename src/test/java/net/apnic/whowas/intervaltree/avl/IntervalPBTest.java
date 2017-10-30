package net.apnic.whowas.intervaltree.avl;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.apnic.whowas.FuncTypeSafeMatchers.encompasses;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(JUnitQuickcheck.class)
public class IntervalPBTest {

    /*
        The natural ordering of CIDR intervals is from least to most specific, given the set of intervals
        encompass a common range.
     */
    @Property
    public void naturalOrderIsLeastToMostSpecific(
            List<
                    @From(Ipv4IntervalGenerator.class)
                    @InRange(min = "10.0.0.0", max = "10.1.0.0")
                    @Ipv4IntervalGenerator.Encompassed(value = "10.0.127.0/24") IpInterval
                    > intervals) {

        assumeThat(intervals, (Matcher) everyItem(encompasses(Parsing.parseCIDRInterval("10.0.127.0/24"))));

        intervals.sort(null);

        List<IpInterval> sortedByPrefix = intervals.stream()
                .sorted((a, b) -> Comparator.<Integer>naturalOrder().compare(a.prefixSize(), b.prefixSize()))
                .collect(Collectors.toList());

        assertThat("Sorted by prefix / network size, i.e. from least specific to most specific",
                intervals,
                is(sortedByPrefix)
        );
    }
}
