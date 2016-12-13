package net.apnic.whowas.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class Domain extends AbstractRdapObject {
    private static JsonNode nameserver(String fqdn) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("ldhName", fqdn);
        node.put("objectClassName", "nameserver");
        return node;
    }

    static Map<String, Object> fromBytes(ObjectKey objectKey, byte[] rpsl, Collection<RdapObject> relatedObjects) {
        Map<String, Object> node = new HashMap<>();

        node.put("objectClassName", "domain");
        node.put("handle", objectKey.getObjectName());
        node.put("ldhName", objectKey.getObjectName());

        if (rpsl.length == 0) {
            node.put("remarks", DELETED_REMARKS);
        } else {
            RpslObject rpslObject = new RpslObject(rpsl);
            node.put("nameservers", new ArrayNode(JsonNodeFactory.instance,
                    rpslObject.getAttribute("nserver").stream()
                            .map(Domain::nameserver)
                            .collect(Collectors.toList())));
            rpslObject.getAttribute("ds-rdata")
                    ;
            // TODO: parse ds-rdata attributes
            node.put("entities", new ArrayList<>(relatedObjects));
            setRemarks(node, rpslObject);
        }

        return node;
    }
}
