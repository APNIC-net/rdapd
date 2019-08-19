package net.apnic.rdapd.rpsl;

import net.apnic.rdapd.types.Tuple;
import org.apache.commons.codec.Charsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An RPSL object
 */
public class RpslObject {
    private final List<Tuple<String, String>> attributes;
    private final List<String> comments;

    public RpslObject(byte[] rpsl) {
        this(new String(rpsl, Charsets.UTF_8));
    }

    public RpslObject(String rpsl) {
        attributes = new ArrayList<>();
        comments = new ArrayList<>();
        List<RpslParser.RpslLineEntry> rpslLineEntries = RpslParser.parseObjectWithComments(rpsl);
        rpslLineEntries.forEach(e -> {
            if (e instanceof RpslParser.RpslAttributeEntry) {
                attributes.add(((RpslParser.RpslAttributeEntry) e).attribute);
            } else if (e instanceof RpslParser.RpslComment) {
                comments.add(((RpslParser.RpslComment) e).comment);
            } else {
                throw new IllegalArgumentException("Unsupported RpslLineEntry type.");
            }
        });
    }

    /**
     * Retrieve an attribute's values
     *
     * @param key the attribute key to look for
     * @return The values; possibly an empty list.
     */
    public List<String> getAttribute(String key) {
        return attributes.stream()
                .filter(o -> o.first().equals(key))
                .map(Tuple::second)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the first value of an attribute
     *
     * @param key the attribute key to look for
     * @return The first value set for the attribute, if any
     */
    public Optional<String> getAttributeFirstValue(String key) {
        return attributes.stream()
                .filter(o -> o.first().equals(key))
                .map(Tuple::second)
                .findFirst();
    }

    /**
     * Get the first attribute of the object.
     *
     * The first attribute typically holds identifying information for the
     * object, though it may not be the unique key of the object.
     *
     * @return The first attribute of the object
     */
    public Tuple<String, String> getPrimaryAttribute() {
        return attributes.get(0);
    }

    /**
     * Get all line comments included in the object.
     * @return a {@link List} of comments
     */
    public List<String> getComments() {
        return comments;
    }
}
