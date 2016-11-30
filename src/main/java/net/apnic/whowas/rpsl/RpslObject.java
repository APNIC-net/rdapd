package net.apnic.whowas.rpsl;

import be.dnsbelgium.rdap.core.Notice;
import net.apnic.whowas.types.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An RPSL object
 */
public class RpslObject {
    private final List<Tuple<String, String>> attributes;

    public RpslObject(byte[] rpsl) {
        attributes = RpslParser.parseObject(rpsl);
    }

    /**
     * Retrieve a list of text attributes as RDAP Notice objects.
     *
     * If there are neither remarks nor descr attributes present,
     * null will be returned rather than an empty list.
     *
     * @return a list of text attributes as RDAP Notice objects.
     */
    public List<Notice> getRemarks() {
        Notice remarks = new Notice("remarks", null, getAttribute("remarks"), null);
        Notice descr   = new Notice("description", null, getAttribute("descr"), null);

        List<Notice> notices = Stream.of(remarks, descr)
                .filter(n -> !n.getDescription().isEmpty())
                .collect(Collectors.toList());

        return notices.isEmpty() ? null : notices;
    }

    /**
     * Retrieve an attribute's values
     *
     * @param key the attribute key to look for
     * @return The values; possibly an empty list.
     */
    public List<String> getAttribute(String key) {
        return attributes.stream()
                .filter(o -> o.fst().equals(key))
                .map(Tuple::snd)
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
                .filter(o -> o.fst().equals(key))
                .map(Tuple::snd)
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

}
