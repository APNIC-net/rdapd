package net.apnic.rdapd.rpsl.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.rdap.AutNum;
import net.apnic.rdapd.rdap.Domain;
import net.apnic.rdapd.rdap.Entity;
import net.apnic.rdapd.rdap.Event;
import net.apnic.rdapd.rdap.GenericObject;
import net.apnic.rdapd.rdap.IpNetwork;
import net.apnic.rdapd.rdap.RdapObject;
import net.apnic.rdapd.rdap.RelatedEntity;
import net.apnic.rdapd.rdap.Role;
import net.apnic.rdapd.rdap.VCard;
import net.apnic.rdapd.rdap.VCardAttribute;
import net.apnic.rdapd.rpsl.Macro;
import net.apnic.rdapd.rpsl.RpslObject;
import net.apnic.rdapd.types.IpInterval;
import net.apnic.rdapd.types.Parsing;
import net.apnic.rdapd.types.Tuple;

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

    @Override
    public RdapObject apply(ObjectKey key, byte[] rpsl)
    {
        return rpslToRdap(key, rpsl);
    }

    private static AutNum autnumFromRpsl(GenericObject autNum, RpslObject rpslObject) {
        AutNum rval = (AutNum) autNum;
        ObjectKey key = rval.getObjectKey();
        Tuple<String, String> autnumSE = parseAutnumRange(key);
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

    private static Domain domainFromRpsl(GenericObject domain, RpslObject rpslObject) {
        Domain rval = (Domain) domain;
        rval.setRelatedEntities(getRelatedEntities(rpslObject));
        rpslObject.getAttribute("nserver").stream()
            .forEach(fqdn -> rval.addNameServer(fqdn));
        rval.setEvents(getEvents(rpslObject));
        rval.setRemarks(getRemarks(rpslObject));

        return rval;
    }

    private static Entity entityFromRpsl(GenericObject entity, RpslObject rpslObject) {
        Entity rval = (Entity) entity;
        rval.setRelatedEntities(getRelatedEntities(rpslObject));

        if (Macro.anyMatches(rpslObject.getComments(), Macro.NO_VCARD)) {
            rval.setVCard(null);
        } else {
            VCard vCard = new VCard();
            EnumSet.allOf(RpslVCardAttribute.class)
                    .stream()
                    .flatMap(a -> a.getProperty(rpslObject))
                    .forEach(vCard::addAttribute);
            rval.setVCard(vCard);
        }

        // TODO: create links
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

    private static IpNetwork ipNetworkFromRpsl(GenericObject ipNetwork, RpslObject rpslObject) {
        IpNetwork rval = (IpNetwork) ipNetwork;
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
    public static RdapObject rpslToRdap(ObjectKey key, byte[] rpsl) {
        RpslObject rpslObject = rpsl.length == 0
                ? null
                : new RpslObject(rpsl);
        return rpslToRdap(key, rpslObject);
    }

    public static RdapObject rpslToRdap(ObjectKey key, RpslObject rpslObject) {
        final GenericObject rval;
        final BiFunction<GenericObject, RpslObject, GenericObject> processRpslObjectFunction;

        switch(key.getObjectClass()) {
            case AUT_NUM:
                rval = new AutNum(key);
                processRpslObjectFunction = RpslToRdap::autnumFromRpsl;
                break;
            case DOMAIN:
                rval = new Domain(key);
                processRpslObjectFunction = RpslToRdap::domainFromRpsl;
                break;
            case ENTITY:
                rval = new Entity(key);
                processRpslObjectFunction = RpslToRdap::entityFromRpsl;
                break;
            case IP_NETWORK:
                IpInterval ipInterval = Parsing.parseInterval(key.getObjectName());
                rval = new IpNetwork(key, ipInterval);
                processRpslObjectFunction = RpslToRdap::ipNetworkFromRpsl;
                break;
            default:
                throw new RuntimeException("Unsupported object class " + key.getObjectClass());
        }

        if (rpslObject == null) {
            return setDeletedState(rval);
        }

        return processRpslObjectFunction.apply(rval, rpslObject);
    }
}
