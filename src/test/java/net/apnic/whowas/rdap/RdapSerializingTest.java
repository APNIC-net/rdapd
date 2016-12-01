package net.apnic.whowas.rdap;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class RdapSerializingTest {
    private static final String BE3_AP =
            "person:         Byron Ellacott\n" +
                    "nic-hdl:        BE3-AP\n" +
                    "e-mail:         bje@apnic.net\n" +
                    "address:        PO Box 3646\n" +
                    "                South Brisbane, QLD 4101, Australia\n" +
                    "phone:          +61-7-3858-3100\n" +
                    "fax-no:         +61-7-3858-3199\n" +
                    "country:        AU\n" +
                    "changed:        bje@apnic.net 20150720\n" +
                    "mnt-by:         MAINT-AU-BE3-AP\n" +
                    "remarks:        A test object for the RDAP history server\n" +
                    "source:         APNIC\n";
    @Test
    public void roundTripTest() throws Exception {
        RdapEntity rdapEntity = new RdapEntity(
                new ObjectKey(ObjectClass.ENTITY, "BE3-AP"),
                BE3_AP.getBytes()
        );
        byte[] foo = RdapSerializing.serialize(rdapEntity);
        ObjectNode bar = RdapSerializing.deserialize(foo, ObjectNode.class);
        assertThat("The object has the right handle", bar.findValue("handle").asText(),
                is(equalTo("BE3-AP")));
        assertEquals("the original and the new match",
                new ObjectNode(JsonNodeFactory.instance, rdapEntity.anyGetter()),
                bar);
    }
}