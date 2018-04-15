
package net.apnic.rdapd.rdap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class RDAPTestingMatches
{
    public static Matcher<String> isRDAPDateTimeString()
    {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(final Object item) {
                return item == null ? true : ((String)item).matches(
                    "^[0-9]{4}(-[0-9]{2}){2}T([0-9]{2}:){2}[0-9]{2}Z$");
            }

            @Override
            public void describeTo(final Description description) {
            }
        };
    }
}
