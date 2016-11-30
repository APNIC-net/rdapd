package net.apnic.whowas.rdap;

import net.apnic.whowas.history.ObjectHistory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An RDAP history response
 */
public class RdapHistory {
    private final List<RdapRecord> records;

    public RdapHistory(Collection<ObjectHistory> objectHistories) {
        records = objectHistories.stream()
                .flatMap(h -> StreamSupport.stream(h.spliterator(), false))
                .map(RdapRecord::new)
                .collect(Collectors.toList());
    }

    public List<RdapRecord> getRecords() {
        return records;
    }

    public RdapHistory(ObjectHistory objectHistory) {
        this(Collections.singleton(objectHistory));
    }
}
