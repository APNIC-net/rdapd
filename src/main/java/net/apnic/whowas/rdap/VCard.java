package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    public Stream<VCardAttribute> findVCardAttribute(String name)
    {
        return attributes.stream()
            .filter(a -> a.getName().equals(name));
    }

    @JsonValue
    public ArrayNode toJSON()
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(getAttributes(), ArrayNode.class);
    }
}
