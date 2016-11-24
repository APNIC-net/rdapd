package net.apnic.whowas.rdap;

import net.apnic.whowas.history.Revision;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * One RDAP object's history record
 */
public class RdapRecord {
    private final ZonedDateTime applicableFrom;
    private final ZonedDateTime applicableUntil;
    private final RdapObject content;

    public RdapRecord(ZonedDateTime applicableFrom, ZonedDateTime applicableUntil, RdapObject content) {
        this.applicableFrom = applicableFrom;
        this.applicableUntil = applicableUntil;
        this.content = content;
    }

    public RdapRecord(Revision revision) {
        this(revision.getValidFrom().withZoneSameInstant(ZoneOffset.UTC), null, revision.getContents());
    }

    public ZonedDateTime getApplicableFrom() {
        return applicableFrom;
    }

    public ZonedDateTime getApplicableUntil() {
        return applicableUntil;
    }

    public RdapObject getContent() {
        return content;
    }
}
