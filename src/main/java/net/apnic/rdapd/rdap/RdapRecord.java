package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.apnic.rdapd.history.Revision;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

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
        this(revision.getValidFrom().withZoneSameInstant(ZoneOffset.UTC),
                Optional.ofNullable(revision.getValidUntil()).map(
                        d -> d.withZoneSameInstant(ZoneOffset.UTC)
                ).orElse(null), revision.getContents());
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    public ZonedDateTime getApplicableFrom() {
        return applicableFrom;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    public ZonedDateTime getApplicableUntil() {
        return applicableUntil;
    }

    public RdapObject getContent() {
        return content;
    }
}
