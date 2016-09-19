package net.apnic.whowas.history;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ObjectFilter {
    private ObjectFilter() {

    }

    public static RpslObject filterAttributes(Predicate<RpslAttribute> filter, RpslObject source) {
        return new RpslObject(source, source.getAttributes().stream().filter(filter).collect(Collectors.toList()));
    }
}
