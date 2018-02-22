package net.apnic.rdapd.history;

import com.github.andrewoma.dexx.collection.IndexedLists;
import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.Vector;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;

/**
 * An object in the history of a registry.
 *
 * An object is identified by a unique number.  Each revision of the object
 * is given a sequence number.
 */
public final class ObjectHistory implements Serializable, Iterable<Revision> {
    private static final long serialVersionUID = 8840997336665340581L;

    // The minimum lifetime of a revision.
    private static final TemporalAmount SQUELCH_TIME = Duration.ofMinutes(300);

    private final ObjectKey objectKey;
    private final transient List<Revision> revisions;

    private ObjectHistory(ObjectKey objectKey, List<Revision> revisions) {
        this.objectKey = objectKey;
        this.revisions = revisions;
    }

    public ObjectHistory(ObjectKey objectKey) {
        this(objectKey, Vector.empty());
    }

    public ObjectHistory appendRevision(Revision revision) {
        List<Revision> newRevisions = Optional.ofNullable(revisions.last())
                .filter(r -> r.getValidUntil() == null || r.getValidUntil().isAfter(revision.getValidFrom()))
                .map(r -> {
                    List<Revision> revs = revisions.take(revisions.size() - 1);
                    // Squelch short-lived revisions
                    if (r.getValidFrom().plus(SQUELCH_TIME).isBefore(revision.getValidFrom())) {
                        return revs.append(r.supersede(revision.getValidFrom()));
                    }
                    return revs;
                })
                .orElse(revisions);
        return new ObjectHistory(objectKey, newRevisions.append(revision));
    }

    public boolean isEmpty() {
        return revisions.size() == 0;
    }

    /**
     * Retieve the most recent and current Revision of the ObjectHistory
     *
     * @return The most recent current Revision, if any.
     */
    public Optional<Revision> mostCurrent() {
        return mostRecent().map(r -> r.getContents().isDeleted() ? null : r);
    }

    /**
     * Retrieve the most recent Revision of the ObjectHistory
     *
     * @return The most recent Revision, if any.
     */
    public Optional<Revision> mostRecent() {
        return Optional.ofNullable(revisions.last());
    }

    /* Iteration and spliteration are provided by the revisions */
    @Override
    public Iterator<Revision> iterator() {
        return revisions.iterator();
    }

    @Override
    public Spliterator<Revision> spliterator() {
        return revisions.spliterator();
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(objectKey, revisions);
    }

    private static class Wrapper implements Serializable {
        private ObjectKey objectKey;
        private Revision[] revisions;

        private Wrapper(ObjectKey objectKey, List<Revision> revisions) {
            this.objectKey = objectKey;
            this.revisions = revisions.toArray(new Revision[0]);
        }

        private Object readResolve() {
            return new ObjectHistory(objectKey, IndexedLists.copyOf(revisions));
        }
    }
}
