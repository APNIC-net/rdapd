package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.core.Notice;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A top level RDAP response, containing server-specific notices and conformance
 * levels.
 */
public class TopLevelObject {
    private static final Set<String> SERVER_CONFORMANCE = Stream.of(
            "rdap_level_0",
            "history_version_0"
    ).collect(Collectors.toSet());
    private static final List<Notice> SERVER_NOTICES = Collections.emptyList();

    private final Set<String> rdapConformance;
    private final List<Notice> notices;

    @JsonUnwrapped
    private final Object object;

    public TopLevelObject(Set<String> rdapConformance, List<Notice> notices, Object object) {
        this.rdapConformance = rdapConformance;
        this.notices = notices;
        this.object = object;
    }

    public Set<String> getRdapConformance() {
        return rdapConformance;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public Object getObject() {
        return object;
    }

    /**
     * Construct a top level object using server defaults
     *
     * @param object The object to wrap
     * @return A top level object using server defaults
     */
    public static TopLevelObject of(Object object) {
        return new TopLevelObject(
                SERVER_CONFORMANCE,
                SERVER_NOTICES,
                object);
    }
}
