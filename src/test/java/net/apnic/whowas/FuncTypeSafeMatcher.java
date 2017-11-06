package net.apnic.whowas;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/*
    http://tadams289.blogspot.com.au/2014/04/hamcrest-matching-with-lambdas.html
 */
public class FuncTypeSafeMatcher<T> extends TypeSafeMatcher<T> {

    Predicate<T>               matchesSafely;
    BiConsumer<T, Description> descibeMismatchSafely;
    Consumer<Description>      describeTo;

    public FuncTypeSafeMatcher(Predicate<T> matchesSafely,
                               Consumer<Description> describeTo,
                               BiConsumer<T, Description> descibeMismatchSafely) {
        this.matchesSafely = matchesSafely;
        this.describeTo = describeTo;
        this.descibeMismatchSafely = descibeMismatchSafely;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return matchesSafely.test(item);
    }

    @Override
    public void describeTo(Description description) {
        describeTo.accept(description);
    }

    @Override
    protected void describeMismatchSafely(T item, Description description) {
        descibeMismatchSafely.accept(item, description);
    }
}
