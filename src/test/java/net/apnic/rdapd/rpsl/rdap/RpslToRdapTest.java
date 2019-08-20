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
}