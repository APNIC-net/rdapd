package net.apnic.whowas.rpsl.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.AutNum;
import net.apnic.whowas.rdap.Domain;
import net.apnic.whowas.rdap.Entity;
import net.apnic.whowas.rdap.Event;
import net.apnic.whowas.rdap.GenericObject;
import net.apnic.whowas.rdap.IpNetwork;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rdap.RelatedEntity;
import net.apnic.whowas.rdap.Role;
import net.apnic.whowas.rdap.VCard;
import net.apnic.whowas.rdap.VCardAttribute;
import net.apnic.whowas.rpsl.RpslObject;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import net.apnic.whowas.types.Tuple;

/**
 * Utility class for converting raw RPSL data into RDAP objects.
 */
public class RpslToRdap
    implements BiFunction<ObjectKey, byte[], RdapObject>
{
    private static final String AUTNUM_RANGE_SEP = "-";

    static final ArrayNode DELETED_REMARKS;
    static {
        ObjectNode notice = new ObjectNode(JsonNodeFactory.instance);
        notice.put("title", "deleted");
        notice.set("description", new ArrayNode(JsonNodeFactory.instance, Collections.singletonList(new TextNode("This object has been deleted"))));
        DELETED_REMARKS = new ArrayNode(JsonNodeFactory.instance, Collections.singletonList(notice));
    }

    private static enum EntityKey
    {
        ADMINC("admin-c", Stream.of(Role.ADMINISTRATIVE).collect(Collectors.toSet())),
        TECHC("tech-c", Stream.of(Role.TECHNICAL).collect(Collectors.toSet())),
        ZONEC("zone-c", Stream.of(Role.TECHNICAL).collect(Collectors.toSet())),
        MAINTAINERC("mnt-irt", Stream.of(Role.ABUSE).collect(Collectors.toSet()));

        private final Set<Role> roles;
        private final String rpslKey;

        EntityKey(String rpslKey, Set<Role> roles)
        {
            this.rpslKey = rpslKey;
            this.roles = roles;
        }

        public Set<Role> getRoles()
        {
            return roles;
        }

        public String getRpslKey()
        {
            return rpslKey;
        }
    }

    private static enum RpslVCardAttribute
    {
        FORMATTED_NAME(o -> Stream.of(makeNode("fn", Collections.emptyMap(), o.getPrimaryAttribute().second()))),

        VCARD_KIND(o -> Stream.of(makeNode("kind", Collections.emptyMap(), getKind(o)))),

        ADDRESS(o -> Stream.of(o.getAttribute("address"))
                .filter(l -> !l.isEmpty())
                .map(l -> String.join("\n", l))
                .map(s -> Collections.singletonMap("label", s))
                .map(p -> makeNode("adr", p, Arrays.asList("", "", "", "", "", "", "")))),

        PHONE_TEL("phone", "tel", Collections.singletonMap("type", "voice")),

        PHONE_FAX("fax-no", "tel", Collections.singletonMap("type", "fax")),

        EMAIL("e-mail", "email"),

        ABUSE_BOX("abuse-mailbox", "email", Collections.singletonMap("pref", "1")),

        ORG("org", "org");

        private final transient Function<RpslObject, Stream<VCardAttribute>> maker;

        RpslVCardAttribute(String attr, String property) {
            this(attr, property, Collections.emptyMap());
        }

        RpslVCardAttribute(String attr, String property, Map<String, String> parameters) {
            this(attr, property, v -> parameters);
        }

        RpslVCardAttribute(String attr, String property, Function<String, Map<String, String>> parameters) {
            this(o -> o.getAttribute(attr).stream().map(v -> makeNode(property, parameters.apply(v), v)));
        }

        RpslVCardAttribute(Function<RpslObject, Stream<VCardAttribute>> maker) {
            this.maker = maker;
        }

        private Stream<VCardAttribute> getProperty(RpslObject rpslObject) {
            return maker.apply(rpslObject);
        }

        private static VCardAttribute makeNode(String key, Map<String, String> params, Object value) {
            return new VCardAttribute(key, params, "text", value);
        }

        private static TextNode getKind(RpslObject rpslObject) {
            switch (rpslObject.getPrimaryAttribute().first()) {
                case "person":
                    return new TextNode("individual");
                case "org":
                    return new TextNode("org");
                default:
                    return new TextNode("group");
            }
        }
    }

    /**
     * @see rpslToRdap()
     */
    @Override
    public RdapObject apply(ObjectKey key, byte[] rpsl)
    {
        return rpslToRdap(key, rpsl);
    }

    /**
     * Converts rpsl data into autnum object.
     *
     * @param key ObjectKey describing the object to be converted
     * @param rpsl Raw RPSL data
     * @return Autnum object
     */
    private static AutNum autnumFromRpsl(ObjectKey key, byte[] rpsl)
    {
        AutNum rval = new AutNum(key);

        if(rpsl.length == 0)
        {
            return setDeletedState(rval);
        }

        Tuple<String, String> autnumSE = parseAutnumRange(key);
        RpslObject rpslObject = new RpslObject(rpsl);
        rval.setRelatedEntities(getRelatedEntities(rpslObject));
        rval.setASNInterval(autnumSE.first(), autnumSE.second());
        rpslObject.getAttributeFirstValue("aut-num")
            .ifPresent(s -> rval.setHandle(s));
        rpslObject.getAttributeFirstValue("as-name")
            .ifPresent(s -> rval.setName(s));
        rpslObject.getAttributeFirstValue("country")
            .ifPresent(s -> rval.setCountry(s));
        rval.setEvents(getEvents(rpslObject));
        rval.setRemarks(getRemarks(rpslObject));

        return rval;
    }

    /**
     * Converts rpsl data into domain object
     *
     * @param key ObjectKey describing the object to be converted
     * @param rpsl Raw RPSL data
     * @return Domain object
     */
    private static Domain domainFromRpsl(ObjectKey key, byte[] rpsl)
    {
        Domain rval = new Domain(key);

        if(rpsl.length == 0)
        {
            return setDeletedState(rval);
        }

        RpslObject rpslObject = new RpslObject(rpsl);
        rval.setRelatedEntities(getRelatedEntities(rpslObject));
        rpslObject.getAttribute("nserver").stream()
            .forEach(fqdn -> rval.addNameServer(fqdn));
        rval.setEvents(getEvents(rpslObject));
        rval.setRemarks(getRemarks(rpslObject));

        return rval;
    }

    /**
     * Entity rpsl data into entity object
     *
     * @param key ObjectKey describing the object to be converted
     * @param rpsl Raw RPSL data
     * @return Entity object
     */
    private static Entity entityFromRpsl(ObjectKey key, byte[] rpsl)
    {
        Entity rval = new Entity(key);

        if(rpsl.length == 0)
        {
            return setDeletedState(rval);
        }

        VCard vCard = new VCard();
        RpslObject rpslObject = new RpslObject(rpsl);
        EnumSet.allOf(RpslVCardAttribute.class)
            .stream()
            .flatMap(a -> a.getProperty(rpslObject))
            .forEach(vCard::addAttribute);
        rval.setRelatedEntities(getRelatedEntities(rpslObject));
        rval.setVCard(vCard);
        rval.setEvents(getEvents(rpslObject));
        rval.setRemarks(getRemarks(rpslObject));

        return rval;
    }

    private static <T extends GenericObject> T setDeletedState(T rdapObject)
    {
        rdapObject.setRemarks(DELETED_REMARKS);
        rdapObject.setDeleted(true);
        return rdapObject;
    }

    private static Collection<RelatedEntity> getRelatedEntities(RpslObject rpslObject)
    {
        HashMap<ObjectKey, RelatedEntity> entities = new HashMap();
        for(EntityKey val : EntityKey.values())
        {
            rpslObject.getAttribute(val.getRpslKey()).stream()
                .forEach(handle ->
                {
                    ObjectKey oKey = new ObjectKey(ObjectClass.ENTITY, handle);
                    RelatedEntity rEntity =
                        entities.getOrDefault(oKey, new RelatedEntity(oKey));
                    rEntity.addRoles(val.getRoles());
                    entities.put(oKey, rEntity);
                });
        }
        return entities.values();
    }

    private static List<Event> getEvents(RpslObject rpslObject)
    {
        return rpslObject.getAttributeFirstValue("last-modified")
            .map(val -> Arrays.asList(
                new Event(Event.EventAction.LAST_CHANGED, val)))
            .orElse(Collections.emptyList());
    }

    private static ArrayNode getRemarks(RpslObject rpslObject)
    {
        List<JsonNode> remarks = Stream.of("description", "remarks")
                .flatMap(a -> {
                    String attrName = a.equals("description") ? "descr" : a;
                    List<JsonNode> notes = rpslObject.getAttribute(attrName)
                            .stream()
                            .map(note -> note.replaceAll("( )+", "$1"))
                            .map(TextNode::new)
                            .collect(Collectors.toList());
                    if (notes.isEmpty()) return Stream.empty();
                    Map<String, JsonNode> kids = new HashMap<>();
                    kids.put("title", new TextNode(a));
                    kids.put("description", new ArrayNode(JsonNodeFactory.instance, notes));
                    return Stream.of(new ObjectNode(JsonNodeFactory.instance, kids));
                }).collect(Collectors.toList());
        return new ArrayNode(JsonNodeFactory.instance, remarks);
    }

    /**
     * Ip network rpsl data into ip network object
     *
     * @param key ObjectKey describing the object to be converted
     * @param rpsl Raw RPSL data
     * @return IpNetwork object
     */
    private static IpNetwork ipNetworkFromRpsl(ObjectKey key, byte[] rpsl)
    {
        IpInterval ipInterval = Parsing.parseInterval(key.getObjectName());
        IpNetwork rval = new IpNetwork(key, ipInterval);

        if(rpsl.length == 0)
        {
            return setDeletedState(rval);
        }

        RpslObject rpslObject = new RpslObject(rpsl);
        rval.setRelatedEntities(getRelatedEntities(rpslObject));
        rpslObject.getAttributeFirstValue("netname")
            .ifPresent(s -> rval.setName(s));
        rpslObject.getAttributeFirstValue("status")
            .ifPresent(s -> rval.setType(s));
        rpslObject.getAttributeFirstValue("country")
            .ifPresent(s -> rval.setCountry(s));
        rval.setEvents(getEvents(rpslObject));
        rval.setRemarks(getRemarks(rpslObject));

        return rval;
    }

    private static String parseAutnum(String autnum)
    {
        int dotIndex = autnum.indexOf(".");
        if (dotIndex > 0) {
            return Long.toString(
                (Long.parseLong(autnum.substring(0, dotIndex)) << 16) +
                Long.parseLong(autnum.substring(dotIndex + 1))).toString();
        } else {
            return autnum;
        }
    }

    private static Tuple<String, String> parseAutnumRange(ObjectKey objectKey)
    {
        String asName = objectKey.getObjectName();
        String startAS = asName;
        String endAS = asName;

        int asRangeIndex = asName.indexOf(AUTNUM_RANGE_SEP);
        if(asRangeIndex > 0)
        {
            startAS = asName.substring(0, asRangeIndex).trim();
            endAS = asName.substring(asRangeIndex + 1).trim();
        }

        return new Tuple<String, String>(parseAutnum(startAS), parseAutnum(endAS));
    }

    /**
     * Translates the supplied rpsl and key to an appropriate RdapObject.
     *
     * @param key ObjectKey describing the rpsl object to be converted
     * @param rpsl Raw RPSL data
     * @return Translated RdapObject
     */
    public static RdapObject rpslToRdap(ObjectKey key, byte[] rpsl)
    {
        switch(key.getObjectClass())
        {
        case AUT_NUM:
            return autnumFromRpsl(key, rpsl);

        case DOMAIN:
            return domainFromRpsl(key, rpsl);

        case ENTITY:
            return entityFromRpsl(key, rpsl);

        case IP_NETWORK:
            return ipNetworkFromRpsl(key, rpsl);

        default:
            throw new RuntimeException("Unsupported object class " +
                key.getObjectClass());
        }
    }
}
