package net.apnic.whowas.history;

import com.github.andrewoma.dexx.collection.IndexedLists;
import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.Vector;

import java.io.ObjectStreamException;
import java.io.Serializable;
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

    private final ObjectKey objectKey;
    private final transient List<Revision> revisions;

    private ObjectHistory(ObjectKey objectKey, List<Revision> revisions) {
        this.objectKey = objectKey;
        this.revisions = revisions;
    }

    public ObjectHistory(ObjectKey objectKey) {
        this(objectKey, Vector.empty());
    }

    ObjectHistory appendRevision(Revision revision) {
        // If the most recent revision doesn't have an "until" date, update it
        List<Revision> newRevisions = Optional.ofNullable(revisions.last())
                .filter(r -> r.getValidUntil() == null || r.getValidUntil().isAfter(revision.getValidFrom()))
                .map(r -> revisions.take(revisions.size() - 1).append(r.supersede(revision.getValidFrom())))
                .orElse(revisions);
        // TODO: squash short-lived revisions
        return new ObjectHistory(objectKey, newRevisions.append(revision));
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
