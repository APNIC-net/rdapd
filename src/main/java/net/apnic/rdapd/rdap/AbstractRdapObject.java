package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.apnic.rdapd.rpsl.RpslObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractRdapObject {
    static void setRemarks(Map<String, Object> node, RpslObject rpslObject) {
        List<JsonNode> remarks = Stream.of("remarks", "description")
                .flatMap(a -> {
                    String attrName = a.equals("description") ? "descr" : a;
                    List<JsonNode> notes = rpslObject.getAttribute(attrName)
                            .stream()
                            .map(TextNode::new)
                            .collect(Collectors.toList());
                    if (notes.isEmpty()) return Stream.empty();
                    Map<String, JsonNode> kids = new HashMap<>();
                    kids.put("title", new TextNode(a));
                    kids.put("description", new ArrayNode(JsonNodeFactory.instance, notes));
                    return Stream.of(new ObjectNode(JsonNodeFactory.instance, kids));
                }).collect(Collectors.toList());
        if (!remarks.isEmpty()) {
            node.put("remarks", new ArrayNode(JsonNodeFactory.instance, remarks));
        }
    }

    static final ArrayNode DELETED_REMARKS;
    static {
        ObjectNode notice = new ObjectNode(JsonNodeFactory.instance);
        notice.put("title", "deleted");
        notice.set("description", new ArrayNode(JsonNodeFactory.instance, Collections.singletonList(new TextNode("This object has been deleted"))));
        DELETED_REMARKS = new ArrayNode(JsonNodeFactory.instance, Collections.singletonList(notice));
    }
}

