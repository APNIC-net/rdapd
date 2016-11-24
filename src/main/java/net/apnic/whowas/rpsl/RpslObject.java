package net.apnic.whowas.rpsl;

import be.dnsbelgium.rdap.core.Notice;
import net.apnic.whowas.types.Tuple;

import java.util.List;
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
     * Retrieve a list of remarks attributes as RDAP Notice objects.
     *
     * @return a list of remarks attributes as RDAP Notice objects.
     */
    public List<Notice> getRemarks() {
        return Stream.of(attributes)
                .map(l -> new Notice("remarks", null, l.stream()
                        .filter(o -> o.fst().equals("remarks"))
                        .map(Tuple::snd)
                        .collect(Collectors.toList()), null))
                .collect(Collectors.toList());
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
