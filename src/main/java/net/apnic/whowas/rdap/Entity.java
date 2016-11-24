package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.core.Notice;
import be.dnsbelgium.vcard.Contact;
import be.dnsbelgium.vcard.datatype.StructuredValue;
import be.dnsbelgium.vcard.datatype.Text;
import be.dnsbelgium.vcard.datatype.Value;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Entity object.
 */
public class Entity implements RdapObject, Serializable {
    private static final long serialVersionUID = -5721385011828987523L;

    @JsonUnwrapped
    private final transient be.dnsbelgium.rdap.core.Entity entity;

    public Entity(ObjectKey objectKey, byte[] rpsl) {
        this(objectKey, rpsl, null);
    }

    public Entity(ObjectKey objectKey, byte[] rpsl, List<be.dnsbelgium.rdap.core.Entity.Role> roles) {
        assert objectKey.getObjectClass() == ObjectClass.ENTITY;

        RpslObject rpslObject = new RpslObject(rpsl);

        // TODO
        // - events:  can I use this for applicability?
        // how to get related objects?

//        person:         Byron Ellacott
//        nic-hdl:        BE3-AP
//        e-mail:         bje@apnic.net
//        address:        PO Box 3646
//        South Brisbane, QLD 4101, Australia
//        phone:          +61-7-3858-3100
//        fax-no:         +61-7-3858-3199
//        country:        AU
//        changed:        bje@apnic.net 20150720
//        mnt-by:         MAINT-AU-BE3-AP
//        remarks:        The lambda (λ) is a Greek character, often used in
//        remarks:        computer science to denote an anonymous function
//        remarks:        (occasionally called a lambda for this same reason)
//        remarks:        because of the lambda calculus, a symbol manipulation
//        remarks:        algebra which expresses computations.
//        remarks:        無名関数を作るためにはラムダを使います
//        source:         APNIC

        Contact contact = new Contact(EnumSet.allOf(VCardAttribute.class)
                .stream()
                .flatMap(a -> a.getProperty(rpslObject))
                .collect(Collectors.toList()));

//        String objectType = attributes.get(0).fst();
//
//        List<Contact.Property> properties = new Contact.Builder()
//                .setFormattedName(attributes.get(0).snd())
//                .build().getProperties();
//
//                Contact.Property.of("fn", Text.of()),
//                Contact.Property.of("kind", Text.of(objectType.equals("person") ? "individual" : "group"))
//        );
//        contact.

        entity = new be.dnsbelgium.rdap.core.Entity(
                /* links */     null,
                /* notices */   null,
                /* remarks */   rpslObject.getRemarks(),
                /* lang */      null,
                be.dnsbelgium.rdap.core.Entity.OBJECT_CLASS_NAME,
                /* events */    null,
                /* status */    null,
                /* port43 */    null,
                /* handle */    objectKey.getObjectName(),
                /* contact */   contact,
                /* roles */     roles,
                /* events */    null,
                /* publicIds */ null
        );
    }

    private Entity(be.dnsbelgium.rdap.core.Entity entity) {
        this.entity = entity;
    }

    private static final List<Notice> DELETED_REMARKS = Collections.singletonList(new Notice(
            "deleted",
            null,
            Collections.singletonList("This object has been deleted"),
            null
    ));


    public static Entity deletedObject(ObjectKey objectKey) {
        return new Entity(new be.dnsbelgium.rdap.core.Entity(
                null, null, DELETED_REMARKS, null, be.dnsbelgium.rdap.core.Entity.OBJECT_CLASS_NAME,
                null, null, null, objectKey.getObjectName(), null, null, null, null));
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
    private static enum VCardAttribute {
        FORMATTED_NAME(o -> Stream.of(Contact.Property.of("fn", new Text(o.getPrimaryAttribute().snd())))),
        VCARD_KIND(o -> Stream.of(Contact.Property.of("kind", getKind(o)))),
        ADDRESS("address", "adr", v -> param("label", v), v -> EMPTY_ADDRESS),
        PHONE_TEL("phone", "tel", param("type", "voice")),
        PHONE_FAX("fax-no", "tel", param("type", "fax")),
        EMAIL("e-mail", "email", v -> null, Text::new),
        ABUSE_BOX("abuse-mailbox", "email", param("pref", "1")),
        ORG("org", "org");

        private final Function<RpslObject, Stream<Contact.Property>> maker;

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
        return new Wrapper(RdapSerializing.serialize(entity));
    }

    private static final class Wrapper implements Serializable {
        private static final long serialVersionUID = -4675825451003583486L;
        private final byte[] data;

        private Wrapper(byte[] data) {
            this.data = data;
        }

        private Object readResolve() {
            return new Entity(RdapSerializing.deserialize(data, be.dnsbelgium.rdap.core.Entity.class));
        }
    }


}
