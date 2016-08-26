package net.apnic.whowas.history;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RpslRecord implements Comparable<RpslRecord> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpslRecord.class);

    // Number of seconds after an ADD before which a DEL or UPD will elide the ADD altogether
    private static final long PRIVATE_THRESHOLD = -60;

    private final int objectType;
    private final String primaryKey;
    private final LocalDateTime whence;
    private final LocalDateTime until;
    private final String raw;
    private final List<RpslRecord> children;

    // for ordering
    private final long objectId;
    private final long sequenceId;

    public RpslRecord(int objectType, String primaryKey, long objectId, long sequenceId,
                      LocalDateTime whence, LocalDateTime until, String raw, List<RpslRecord> children) {
        this.objectType = objectType;
        this.primaryKey = primaryKey;
        this.whence = whence;
        this.until = until;
        this.raw = raw;
        this.children = children;
        this.objectId = objectId;
        this.sequenceId = sequenceId;
    }

    /**
     * Cleft an RpslRecord in twain around the given time.
     *
     * Ensure that this.getWhence() <= thence <= this.getUntil() or the result is meaningless.
     *
     * @param thence The time around which to split
     * @return An array of two new RpslRecords, split at the given time
     */
    public RpslRecord[] splitAt(LocalDateTime thence) {
        return new RpslRecord[] {
                new RpslRecord(this.objectType, this.primaryKey, this.objectId,
                        this.sequenceId, this.whence, thence, this.raw, this.children),
                new RpslRecord(this.objectType, this.primaryKey, this.objectId,
                        this.sequenceId, thence, this.until, this.raw, this.children)
        };
    }

    public RpslRecord withChild(RpslRecord child) {
        List<RpslRecord> newChildren = new ArrayList<>(this.children.size() + 1);
        newChildren.addAll(this.children);
        newChildren.add(child);
        return new RpslRecord(this.objectType, this.primaryKey, this.objectId,
                this.sequenceId, this.whence, this.until, this.raw, newChildren);
    }

    public RpslRecord withUntil(LocalDateTime until) {
        return new RpslRecord(this.objectType, this.primaryKey, this.objectId,
                this.sequenceId, this.whence, until, this.raw, this.children);
    }

    public Optional<RpslObject> getRpslObject() {
        if (raw.isEmpty()) {
            return Optional.empty();
        }

        // RpslObject.parse() might throw a runtime exception, because THOSE ARE GREAT!
        try {
            // Good data, bad data, I'm the data with the fake tabs
            String workaround = raw.replace("\\t", "\t");
            return Optional.of(RpslObject.parse(workaround));
        } catch (IllegalArgumentException wut) {
            LOGGER.error("Exception parsing RPSL data", wut);
            return Optional.empty();
        }
    }

    public int getObjectType() {
        return objectType;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public LocalDateTime getWhence() {
        return whence;
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public String getRaw() {
        return raw;
    }

    public boolean isDelete() {
        return raw.isEmpty();
    }

    public boolean isPrivate() {
        return sequenceId == 1 && whence.until(until, ChronoUnit.SECONDS) < PRIVATE_THRESHOLD;
    }

    public List<RpslRecord> getChildren() {
        // safe because all elements of this list are themselves immutable
        return Collections.unmodifiableList(children);
    }

    @Override
    public String toString() {
        return String.format("[%s - %s) %d: %s (%d children, %d bytes)",
                whence, until, objectType, primaryKey, children.size(), raw.length());
    }

    @Override
    public int compareTo(RpslRecord o) {
        long gap = objectId - o.objectId;
        if (gap == 0) {
            // sequence ID 0 is the biggest, otherwise natural number order applies
            if (sequenceId == 0) {
                gap = 1;
            } else if (o.sequenceId == 0) {
                gap = -1;
            } else {
                gap = sequenceId - o.sequenceId;
            }
        }
        if (gap == 0) {
            gap = whence.compareTo(o.whence);
        }
        return Long.signum(gap);
    }

    @Override
    public int hashCode() {
        return 31 + 31 * Long.hashCode(objectId) + 31 * Long.hashCode(sequenceId) + whence.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof RpslRecord && compareTo((RpslRecord)o) == 0;
    }
}
