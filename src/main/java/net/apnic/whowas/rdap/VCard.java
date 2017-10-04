package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VCard
    implements Serializable
{
    private final ArrayList<VCardAttribute> attributes = new ArrayList<>();

    public void addAttribute(VCardAttribute attribute)
    {
        attributes.add(attribute);
    }

    public List<VCardAttribute> getAttributes()
    {
        return attributes;
    }

    @JsonValue
    public ArrayNode toJSON()
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(getAttributes(), ArrayNode.class);
    }
}
