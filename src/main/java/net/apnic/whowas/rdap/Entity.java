package net.apnic.whowas.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An Entity object.
 */
class Entity extends AbstractRdapObject {
    static Map<String, Object> fromBytes(ObjectKey objectKey, byte[] rpsl, Collection<RdapObject> relatedObjects) {
        Map<String, Object> node = new HashMap<>();

        node.put("objectClassName", "entity");
        node.put("handle", objectKey.getObjectName());

        if (rpsl.length == 0) {
            node.put("remarks", DELETED_REMARKS);
        } else {
            RpslObject rpslObject = new RpslObject(rpsl);
            node.put("vcardArray", new ArrayNode(JsonNodeFactory.instance,
                    Arrays.asList(new TextNode("vcard"),
                            new ArrayNode(JsonNodeFactory.instance,
                                    EnumSet.allOf(VCardAttribute.class)
                                            .stream()
                                            .flatMap(a -> a.getProperty(rpslObject))
                                            .collect(Collectors.toList())))));
            setRemarks(node, rpslObject);
        }

        return node;
    }

    /* VCard attributes */
    private static final transient JsonNode EMPTY_ADDRESS = new ArrayNode(JsonNodeFactory.instance,
            IntStream.range(0, 7).mapToObj(i -> new TextNode("")).collect(Collectors.toList()));

    @SuppressWarnings(/* They're used, just not by name. */ "unused")
    private enum VCardAttribute {
        VERSION(o -> Stream.of(makeNode("version", Collections.emptyMap(), new TextNode("4.0")))),
        FORMATTED_NAME(o -> Stream.of(makeNode("fn", Collections.emptyMap(), new TextNode(o.getPrimaryAttribute().snd())))),
        VCARD_KIND(o -> Stream.of(makeNode("kind", Collections.emptyMap(), getKind(o)))),
        ADDRESS(o -> Stream.of(o.getAttribute("address"))
                .filter(l -> !l.isEmpty())
                .map(l -> String.join("\n", l))
                .map(s -> Collections.singletonMap("label", s))
                .map(p -> makeNode("adr", p, EMPTY_ADDRESS))),
        PHONE_TEL("phone", "tel", Collections.singletonMap("type", "voice")),
        PHONE_FAX("fax-no", "tel", Collections.singletonMap("type", "fax")),
        EMAIL("e-mail", "email"),
        ABUSE_BOX("abuse-mailbox", "email", Collections.singletonMap("pref", "1")),
        ORG("org", "org");

        private final transient Function<RpslObject, Stream<JsonNode>> maker;

        VCardAttribute(String attr, String property) {
            this(attr, property, Collections.emptyMap());
        }

        VCardAttribute(String attr, String property, Map<String, String> parameters) {
            this(attr, property, v -> parameters, TextNode::new);
        }

        VCardAttribute(String attr, String property, Function<String, Map<String, String>> parameters, Function<String, JsonNode> value) {
            this(o -> o.getAttribute(attr).stream().map(v -> makeNode(property, parameters.apply(v), value.apply(v))));
        }

        VCardAttribute(Function<RpslObject, Stream<JsonNode>> maker) {
            this.maker = maker;
        }

        private Stream<JsonNode> getProperty(RpslObject rpslObject) {
            return maker.apply(rpslObject);
        }

        private static JsonNode makeNode(String key, Map<String, String> params, JsonNode value) {
            ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
            arrayNode.add(key);
            arrayNode.add(new ObjectNode(JsonNodeFactory.instance, params.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new TextNode(e.getValue())))));
            arrayNode.add("text");
            arrayNode.add(value);
            return arrayNode;
        }

        private static TextNode getKind(RpslObject rpslObject) {
            switch (rpslObject.getPrimaryAttribute().fst()) {
                case "person":
                    return new TextNode("individual");
                case "org":
                    return new TextNode("org");
                default:
                    return new TextNode("group");
            }
        }
    }
}
