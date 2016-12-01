package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.Notice;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.patches.VersionedIpNetwork;
import net.apnic.whowas.rpsl.RpslObject;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An IP network object.
 */
public class IpNetwork implements RdapObject, Serializable {
    private static final long serialVersionUID = -5015613771361131756L;

//    @JsonUnwrapped
    private final transient ObjectNode ipNetwork;

    private final ObjectKey objectKey;

    private final Map<ObjectKey, EnumSet<Entity.Role.Default>> relatedObjects;

    public IpNetwork(ObjectKey objectKey, byte[] rpsl) {
        assert objectKey.getObjectClass() == ObjectClass.IP_NETWORK;

        this.objectKey = objectKey;
        this.relatedObjects = new HashMap<>();

        RpslObject rpslObject = new RpslObject(rpsl);

        // TODO
        // - links: self
        // - links: "head"/"current" if available
        // - events:  can I use this for applicability?

        // Extract related objects
        for (Map.Entry<String, Entity.Role.Default> entry : RELATED_ROLES.entrySet()) {
            for (String key : rpslObject.getAttribute(entry.getKey())) {
                ObjectKey relatedKey = new ObjectKey(ObjectClass.ENTITY, key);
                relatedObjects.putIfAbsent(relatedKey, EnumSet.noneOf(Entity.Role.Default.class));
                EnumSet<Entity.Role.Default> relations = relatedObjects.get(relatedKey);
                relations.add(entry.getValue());
                relatedObjects.put(relatedKey, relations);
            }
        }

        IpInterval ipInterval = Parsing.parseInterval(objectKey.getObjectName());
        VersionedIpNetwork ipNetwork = new VersionedIpNetwork(
                /* links */     null,
                /* notices */   null,
                /* remarks */   rpslObject.getRemarks(),
                /* lang */      null,
                /* events */    null,
                /* status */    null,
                /* port43 */    null,
                /* handle */    objectKey.getObjectName(),
                /* start */     ipInterval.low().getAddress(),
                /* end */       ipInterval.high().getAddress(),
                /* ipVersion */ ipInterval.low().getAddressFamily() == IP.AddressFamily.IPv4 ? "v4" : "v6",
                /* name */      rpslObject.getAttributeFirstValue("netname").orElse(null),
                /* type */      rpslObject.getAttributeFirstValue("status").orElse(null),
                /* country */   rpslObject.getAttributeFirstValue("country").orElse(null),
                /* parent */    null,
                /* entities */  null
        );

        this.ipNetwork = RdapSerializing.valueToTree(ipNetwork);
    }

    @Override
    public ObjectKey getObjectKey() {
        return objectKey;
    }

    @Override
    public Collection<ObjectKey> getEntityKeys() {
        return relatedObjects.keySet();
    }

    @JsonAnyGetter
    @SuppressWarnings("unused")
    public Map<String, JsonNode> anyGetter() {
        HashMap<String, JsonNode> result = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> entries = ipNetwork.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> entry = entries.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public RdapObject withEntities(Collection<RdapObject> entities) {
        ObjectNode newNode = ipNetwork.deepCopy();
        if (entities.isEmpty()) {
            newNode.remove("entities");
        } else {
            newNode.replace("entities",
                    new ArrayNode(JsonNodeFactory.instance, entities.stream()
                            .filter(o -> relatedObjects.containsKey(o.getObjectKey()))
                            .map(e -> addRoles(RdapSerializing.valueToTree(e), relatedObjects.get(e.getObjectKey())))
                            .collect(Collectors.toList())));
        }

        return new IpNetwork(relatedObjects, objectKey, newNode);
    }

    private JsonNode addRoles(ObjectNode object, Collection<? extends Entity.Role> roles) {
        ObjectNode newNode = object.deepCopy();
        if (roles.isEmpty()) {
            newNode.remove("roles");
        } else {
            newNode.replace("roles",
                    new ArrayNode(JsonNodeFactory.instance, roles.stream()
                            .map(Entity.Role::getValue)
                            .map(TextNode::new)
                            .collect(Collectors.toList())));
        }
        return newNode;
    }

    private IpNetwork(Map<ObjectKey, EnumSet<Entity.Role.Default>> relatedObjects, ObjectKey objectKey, VersionedIpNetwork ipNetwork) {
        this(relatedObjects, objectKey, RdapSerializing.valueToTree(ipNetwork));
    }

    private IpNetwork(Map<ObjectKey, EnumSet<Entity.Role.Default>> relatedObjects, ObjectKey objectKey, ObjectNode ipNetwork) {
        this.relatedObjects = relatedObjects;
        this.objectKey = objectKey;
        this.ipNetwork = ipNetwork;
    }

    private static final List<Notice> DELETED_REMARKS = Collections.singletonList(new Notice(
            "deleted",
            null,
            Collections.singletonList("This object has been deleted"),
            null
    ));


    public static IpNetwork deletedObject(ObjectKey objectKey) {
        IpInterval ipInterval = Parsing.parseInterval(objectKey.getObjectName());
        return new IpNetwork(new HashMap<>(), objectKey, new VersionedIpNetwork(
                null, null, DELETED_REMARKS, null,
                null, null, null, objectKey.getObjectName(), ipInterval.low().getAddress(),
                ipInterval.high().getAddress(),
                ipInterval.low().getAddressFamily() == IP.AddressFamily.IPv4 ? "v4" : "v6",
                null, null, null, null, null));
    }

    private static final Map<String, Entity.Role.Default> RELATED_ROLES = new HashMap<>();
    static {
        RELATED_ROLES.put("admin-c", Entity.Role.Default.ADMINISTRATIVE);
        RELATED_ROLES.put("tech-c", Entity.Role.Default.TECHNICAL);
        RELATED_ROLES.put("mnt-irt", Entity.Role.Default.ABUSE);
        RELATED_ROLES.put("zone-c", Entity.Role.Default.TECHNICAL);
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(relatedObjects, objectKey, RdapSerializing.serialize(ipNetwork));
    }

    private static final class Wrapper implements Serializable {
        private static final long serialVersionUID = -8825651861679233094L;
        private final Map<ObjectKey, EnumSet<Entity.Role.Default>> relatedObjects;
        private final ObjectKey objectKey;
        private final byte[] data;

        private Wrapper(Map<ObjectKey, EnumSet<Entity.Role.Default>> relatedObjects, ObjectKey objectKey, byte[] data) {
            this.relatedObjects = relatedObjects;
            this.objectKey = objectKey;
            this.data = data;
        }

        private Object readResolve() {
            return new IpNetwork(relatedObjects, objectKey, RdapSerializing.deserialize(data, ObjectNode.class));
        }
    }

}
