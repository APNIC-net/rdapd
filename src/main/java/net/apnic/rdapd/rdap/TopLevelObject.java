package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A top level RDAP response, containing server notices and conformance.
 */
public class TopLevelObject {
    private static final String HISTORY_VERSION_O = "history_version_0";
    private static final Set<String> SERVER_CONFORMANCE = Stream.of(
            "rdap_level_0",
            "cidr0"
    ).collect(Collectors.toSet());

    private final Set<String> rdapConformance;
    private final List<Notice> notices;
    private final Object object;
    private final String port43;

    private TopLevelObject(Set<String> rdapConformance, List<Notice> notices,
                           String port43, Object object) {
        this.rdapConformance = rdapConformance;
        this.notices = notices;
        this.port43 = port43;
        this.object = object;
    }

    public Set<String> getRdapConformance() {
        return rdapConformance;
    }

    @JsonUnwrapped
    public Object getObject() {
        return object;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPort43()
    {
        return port43;
    }

    /**
     * Construct a top level object using server defaults
     *
     * @param object The object to wrap
     * @return A top level object using server defaults
     */
    public static TopLevelObject of(Object object, Notice notice, String port43)
    {
        List<Notice> notices = new ArrayList<>();

        if (notice != null) {
            notices.add(notice);
        }

        return of(object, notices, port43);
    }

    /**
     * Construct a top level object using server defaults
     *
     * @param object The object to wrap
     * @return A top level object using server defaults
     */
    public static TopLevelObject of(Object object, List<Notice> notices,
                                    String port43)
    {
        return new TopLevelObject(
                SERVER_CONFORMANCE,
                notices,
                port43,
                object);
    }

    public static void setHistoryConformance(boolean enabled) {
        if (enabled) {
            SERVER_CONFORMANCE.add(HISTORY_VERSION_O);
        } else {
            SERVER_CONFORMANCE.remove(HISTORY_VERSION_O);
        }
    }
}
