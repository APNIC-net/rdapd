package net.apnic.whowas.rdap;

import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Static method for constructing AutNum objects from WHOIS aut-num records
 */
class AutNum extends AbstractRdapObject{
    private static long parseAutNum(ObjectKey objectKey) {
        int dotIndex = objectKey.getObjectName().indexOf(".");
        if (dotIndex > 0) {
            return (Long.parseLong(objectKey.getObjectName().substring(2, dotIndex)) << 16)
                    + Long.parseLong(objectKey.getObjectName().substring(dotIndex + 1));
        } else {
            return Long.parseLong(objectKey.getObjectName().substring(2));
        }
    }

    static Map<String, Object> fromBytes(ObjectKey objectKey, byte[] rpsl, Collection<RdapObject> relatedObjects) {
        Map<String, Object> node = new HashMap<>();

        long autnum = parseAutNum(objectKey);
        node.put("objectClassName", "autnum");
        node.put("handle", objectKey.getObjectName());
        node.put("startAutnum", autnum);
        node.put("endAutnum", autnum);

        if (rpsl.length == 0) {
            node.put("remarks", DELETED_REMARKS);
        } else {
            RpslObject rpslObject = new RpslObject(rpsl);
            rpslObject.getAttributeFirstValue("as-name").ifPresent(s -> node.put("name", s));
            rpslObject.getAttributeFirstValue("country").ifPresent(s -> node.put("country", s));
            node.put("entities", new ArrayList<>(relatedObjects));
            setRemarks(node, rpslObject);
        }

        return node;
    }
}
