package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.Notice;
import be.dnsbelgium.vcard.Contact;
import be.dnsbelgium.vcard.datatype.StructuredValue;
import be.dnsbelgium.vcard.datatype.Text;
import be.dnsbelgium.vcard.datatype.Value;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Entity object.
 */
public class RdapEntity implements RdapObject, Serializable {
    private static final long serialVersionUID = -5721385011828987523L;

    private final transient ObjectNode entity;

    private final ObjectKey objectKey;

    public RdapEntity(ObjectKey objectKey, byte[] rpsl) {
        assert objectKey.getObjectClass() == ObjectClass.ENTITY;

        this.objectKey = objectKey;

        RpslObject rpslObject = new RpslObject(rpsl);

        // TODO
        // - events:  can I use this for applicability?
        // how to get related objects?

        Contact contact = new Contact(EnumSet.allOf(VCardAttribute.class)
                .stream()
                .flatMap(a -> a.getProperty(rpslObject))
                .collect(Collectors.toList()));

        Entity entity = new Entity(
                /* links */     null,
                /* notices */   null,
                /* remarks */   rpslObject.getRemarks(),
                /* lang */      null,
                Entity.OBJECT_CLASS_NAME,
                /* events */    null,
                /* status */    null,
                /* port43 */    null,
                /* handle */    objectKey.getObjectName(),
                /* contact */   contact,
                /* roles */     null,
                /* events */    null,
                /* publicIds */ null
        );

        this.entity = RdapSerializing.valueToTree(entity);
    }

    private RdapEntity(ObjectKey objectKey, Entity entity) {
        this(objectKey, RdapSerializing.valueToTree(entity));
    }

    private RdapEntity(ObjectKey objectKey, ObjectNode entity) {
        this.objectKey = objectKey;
        this.entity = entity;
    }

    @Override
    public ObjectKey getObjectKey() {
        return objectKey;
    }

    @JsonAnyGetter
    @SuppressWarnings("unused")
    public Map<String, JsonNode> anyGetter() {
        HashMap<String, JsonNode> result = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> entries = entity.fields();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> entry = entries.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static final List<Notice> DELETED_REMARKS = Collections.singletonList(new Notice(
            "deleted",
            null,
            Collections.singletonList("This object has been deleted"),
            null
    ));


    public static RdapEntity deletedObject(ObjectKey objectKey) {
        return new RdapEntity(objectKey, new Entity(
                null, null, DELETED_REMARKS, null, Entity.OBJECT_CLASS_NAME,
                null, null, null, objectKey.getObjectName(), null,
                null, null, null));
    }

    private static Text getKind(RpslObject rpslObject) {
        switch (rpslObject.getPrimaryAttribute().fst()) {
            case "person":
                return new Text("individual");
            case "org":
                return new Text("org");
            default:
                return new Text("group");
        }
    }

    private static Contact.Parameters param(String k, String v) {
        return new Contact.Parameters.Builder().add(k, v).build();
    }

    /* VCard attributes */
    private static final transient Value EMPTY_ADDRESS = StructuredValue.ADRType.of("", "","", "", "", "", "");

    @SuppressWarnings(/* They're used, just not by name. */ "unused")
    private enum VCardAttribute {
        FORMATTED_NAME(o -> Stream.of(Contact.Property.of("fn", new Text(o.getPrimaryAttribute().snd())))),
        VCARD_KIND(o -> Stream.of(Contact.Property.of("kind", getKind(o)))),
        ADDRESS(o -> Stream.of(o.getAttribute("address"))
                .filter(l -> !l.isEmpty())
                .map(l -> String.join("\n", l))
                .map(s -> param("label", s))
                .map(p -> Contact.Property.of("adr", p, EMPTY_ADDRESS))),
//        ADDRESS("address", "adr", v -> param("label", v), v -> EMPTY_ADDRESS),
        PHONE_TEL("phone", "tel", param("type", "voice")),
        PHONE_FAX("fax-no", "tel", param("type", "fax")),
        EMAIL("e-mail", "email"),
        ABUSE_BOX("abuse-mailbox", "email", param("pref", "1")),
        ORG("org", "org");

        private final transient Function<RpslObject, Stream<Contact.Property>> maker;

        VCardAttribute(String attr, String property) {
            this(attr, property, null);
        }

        VCardAttribute(String attr, String property, Contact.Parameters parameters) {
            this(attr, property, v -> parameters, Text::new);
        }

        VCardAttribute(String attr, String property, Function<String, Contact.Parameters> parameters, Function<String, Value> value) {
            this(o -> o.getAttribute(attr).stream().map(v -> Contact.Property.of(property, parameters.apply(v), value.apply(v))));
        }

        VCardAttribute(Function<RpslObject, Stream<Contact.Property>> maker) {
            this.maker = maker;
        }

        private Stream<Contact.Property> getProperty(RpslObject rpslObject) {
            return maker.apply(rpslObject);
        }
    }

    /****
     * Serialization code below
     ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(objectKey, RdapSerializing.serialize(entity));
    }

    private static final class Wrapper implements Serializable {
        private static final long serialVersionUID = -4675825451003583486L;
        private final ObjectKey objectKey;
        private final byte[] data;

        private Wrapper(ObjectKey objectKey, byte[] data) {
            this.objectKey = objectKey;
            this.data = data;
        }

        private Object readResolve() {
            return new RdapEntity(objectKey, RdapSerializing.deserialize(data, ObjectNode.class));
        }
    }


}
