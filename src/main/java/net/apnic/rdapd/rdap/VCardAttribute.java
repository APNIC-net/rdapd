package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class VCardAttribute
    implements Serializable
{
    public static final VCardAttribute VERSION_ATTRIBUTE;

    static
    {
        VERSION_ATTRIBUTE = new VCardAttribute("version", "text", "4.0");
    }

    private final String name;
    private final Map<String, String> parameters;
    private final String type;
    private final Object value;

    public VCardAttribute(String name, String type, Object value)
    {
        this(name, Collections.emptyMap(), type, value);
    }

    public VCardAttribute(String name, Map<String, String> parameters,
                          String type, Object value)
    {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public String getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }

    @JsonValue
    public ArrayNode toJSON()
    {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode node = new ArrayNode(JsonNodeFactory.instance);
        node.add(getName());
        node.add(mapper.valueToTree(getParameters()));
        node.add(getType());
        node.add(mapper.convertValue(getValue(), JsonNode.class));
        return node;
    }
}
