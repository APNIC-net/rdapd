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
    private static final long serialVersionUID = 2019509428869073107L;

    private final transient ObjectKey objectKey;
    private final transient List<Revision> revisions;

    private ObjectHistory(ObjectKey objectKey, List<Revision> revisions) {
        this.objectKey = objectKey;
        this.revisions = revisions;
    }

    public ObjectHistory(ObjectKey objectKey) {
        this(objectKey, Vector.empty());
    }

    ObjectHistory appendRevision(Revision revision) {
        return new ObjectHistory(objectKey, revisions.append(revision));
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
