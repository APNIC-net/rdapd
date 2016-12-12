package net.apnic.whowas.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RdapDomain extends AbstractRdapObject implements Serializable {
    public RdapDomain(ObjectKey objectKey, byte[] rpsl) {
        super(objectKey, rpsl);
    }

    @Override
    protected ObjectNode makeObjectNode(RpslObject rpslObject) {
        ObjectNode domain = new ObjectNode(JsonNodeFactory.instance);
        domain.put("objectClassName", "domain");
        domain.put("handle", objectKey.getObjectName());
        domain.put("ldhName", objectKey.getObjectName());
        domain.set("nameservers", new ArrayNode(JsonNodeFactory.instance,
                rpslObject.getAttribute("nserver").stream()
                        .map(RdapDomain::nameserver)
                        .collect(Collectors.toList())));
        setRemarks(domain, rpslObject);
        return domain;
    }

    private static JsonNode nameserver(String fqdn) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("ldhName", fqdn);
        node.put("objectClassName", "nameserver");
        return node;
    }

    RdapDomain(Map<ObjectKey, Set<String>> relatedObjects, ObjectKey objectKey, ObjectNode domain) {
        super(relatedObjects, objectKey, domain);
    }

    public static RdapDomain deletedObject(ObjectKey objectKey) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);

        node.put("objectClassName", "domain");
        node.put("handle", objectKey.getObjectName());
        node.put("ldhName", objectKey.getObjectName());
        node.set("remarks", DELETED_REMARKS);

        return new RdapDomain(new HashMap<>(), objectKey, node);
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(relatedObjects, objectKey, RdapSerializing.serialize(objectNode));
    }

    private static final class Wrapper implements Serializable {
        private static final long serialVersionUID = -8825651861679233094L;
        private final Map<ObjectKey, Set<String>> relatedObjects;
        private final ObjectKey objectKey;
        private final byte[] data;

        private Wrapper(Map<ObjectKey, Set<String>> relatedObjects, ObjectKey objectKey, byte[] data) {
            this.relatedObjects = relatedObjects;
            this.objectKey = objectKey;
            this.data = data;
        }

        private Object readResolve() {
            return new RdapDomain(relatedObjects, objectKey, RdapSerializing.deserialize(data, ObjectNode.class));
        }
    }

}
