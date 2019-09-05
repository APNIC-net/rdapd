package net.apnic.rdapd.rpsl.rdap;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.rdap.Entity;
import net.apnic.rdapd.rdap.RdapObject;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class RpslToRdapTest {

    @Test
    public void testNoVCardMacro() {
        // Given
        final String rpslData = "# NO_VCARD\n" +
                "person:  Example Citizen\n" +
                "remarks: a continuation\n+\n+     line.\n" +
                "source:\t\tTEST\n" +
                "nic-hdl: TEST";

        // When
        RdapObject rdapObject = RpslToRdap.rpslToRdap(
                new ObjectKey(ObjectClass.ENTITY, "TEST"),
                rpslData.getBytes());

        // Then
        assertThat(((Entity) rdapObject).getVCard(), is(nullValue()));
    }

    @Test
    public void testLinksMacro() {
        // Given
        final String rpslData = "# LINK: { \"href\": \"http://whois.nic.ad.jp/cgi-bin/whois_gw?key= JP00010080\", " +
                "\"rel\": \"related\", \"hreflang\": \"jp\", \"type\": \"text/html\" }\n" +
                "# LINK: { \"href\": \"http://whois.nic.ad.jp/cgi-bin/whois_gw?key= JP00010080/e\", " +
                "\"rel\": \"related\", \"hreflang\": \"en\", \"type\": \"text/html\" }\n" +
                "person:  Example Citizen\n" +
                "remarks: a continuation\n+\n+     line.\n" +
                "source:\t\tTEST\n" +
                "nic-hdl: TEST";

        // When
        Entity entity = (Entity) RpslToRdap.rpslToRdap(
                new ObjectKey(ObjectClass.ENTITY, "TEST"),
                rpslData.getBytes());

        // Then
        assertThat(entity, is(notNullValue()));
        assertThat(entity.getLinks(), is(notNullValue()));
        assertThat(entity.getLinks().size(), is(2));
        assertThat(entity.getLinks().get(0).getHref(), is("http://whois.nic.ad.jp/cgi-bin/whois_gw?key= JP00010080"));
        assertThat(entity.getLinks().get(0).getRel(), is("related"));
        assertThat(entity.getLinks().get(0).getType(), is("text/html"));
        assertThat(entity.getLinks().get(0).getHreflang(), is("jp"));
        assertThat(entity.getLinks().get(1).getHref(), is("http://whois.nic.ad.jp/cgi-bin/whois_gw?key= JP00010080/e"));
        assertThat(entity.getLinks().get(1).getRel(), is("related"));
        assertThat(entity.getLinks().get(1).getType(), is("text/html"));
        assertThat(entity.getLinks().get(1).getHreflang(), is("en"));
    }
}