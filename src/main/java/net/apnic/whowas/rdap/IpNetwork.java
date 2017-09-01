package net.apnic.whowas.rdap;

import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An IP network object.
 */
class IpNetwork extends AbstractRdapObject {
    static Map<String, Object> fromBytes(ObjectKey objectKey, byte[] rpsl, Collection<RdapObject> relatedObjects) {
        IpInterval ipInterval = Parsing.parseInterval(objectKey.getObjectName());

        Map<String, Object> node = new HashMap<>();

        node.put("objectClassName", "ip network");
        node.put("handle", objectKey.getObjectName());
        node.put("startAddress", ipInterval.low().getAddress().getHostAddress());
        node.put("endAddress", ipInterval.high().getAddress().getHostAddress());
        node.put("ipVersion", ipInterval.low().getAddressFamily() == IP.AddressFamily.IPv4 ? "v4" : "v6");

        if (rpsl.length == 0) {
            node.put("remarks", DELETED_REMARKS);
        } else {
            RpslObject rpslObject = new RpslObject(rpsl);
            rpslObject.getAttributeFirstValue("netname").ifPresent(s -> node.put("name", s));
            rpslObject.getAttributeFirstValue("status").ifPresent(s -> node.put("type", s));
            rpslObject.getAttributeFirstValue("country").ifPresent(s -> node.put("country", s));
            node.put("entities", new ArrayList<>(relatedObjects));
            setRemarks(node, rpslObject);
        }

        return node;
    }
}
