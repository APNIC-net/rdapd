package net.apnic.whowas.history;

import com.github.andrewoma.dexx.collection.*;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.intervaltree.avl.AvlTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Optional;

/**
 * The History of a registry.
 *
 * The history of a registry is the history of each object within the registry,
 * a serial number reflecting the version of the registry, and a set of indices
 * for fast interval lookups.
 */
public final class History implements Externalizable {
    private static final long serialVersionUID = -1090748880048059931L;
    private static final Logger LOGGER = LoggerFactory.getLogger(History.class);

    /* The histories of all objects, and the tree of IP intervals */
    private volatile long serial;
    private volatile Map<ObjectKey, ObjectHistory> histories;
    private volatile AvlTree<IP, ObjectKey, IpInterval> tree;

    /**
     * Construct a new History in which nothing has ever happened.
     */
    public History() {
        histories = HashMap.empty();
        tree = new AvlTree<>();
    }

    /**
     * Horrible way to deserialize a History.
     *
     * App uses a final instance variable for its history.
     *
     * @param history the shameful history of past of software design choices
     */
    public synchronized void deserialize(History history) {
        this.serial = history.serial;
        this.histories = history.histories;
        this.tree = history.tree;
    }

    /**
     * Update an object in the History with a new revision.
     *
     * @param objectKey The object to append the new revision onto
     * @param revision The new revision of the object
     */
    public synchronized void addRevision(ObjectKey objectKey, Revision revision) {
        // The trick is to do this operation without messing with in-progress
        // queries, without incurring the cost of @synchronised locking
        // everywhere, and without quadratic performance during initial loads.
        AvlTree<IP, ObjectKey, IpInterval> nextTree = tree;

        // Obtain a new object history with this revision included
        ObjectHistory objectHistory = histories.get(objectKey);
        if (objectHistory == null) {
            objectHistory = new ObjectHistory(objectKey);
            // TODO: Create a new tree if this is a new INET(6)NUM object
            if (objectKey.getObjectClass() == ObjectClass.IP_NETWORK) {
                try {
                    IpInterval interval = Parsing.parseInterval(objectKey.getObjectName());
                    nextTree = tree.update(interval, objectKey, (a,b) -> {
                        assert a.equals(b);
                        return a;
                    }, o -> o);
                } catch (Exception ex) {
                    // Use the short exception message, don't need a stack trace in the logs!
                    LOGGER.warn("Object {} not added to IP tree: parse exception {}", objectKey, ex.getMessage());
                    // absorb and move on
                }
            }
        }

        // TODO: filter revisions at this point
        //  - Subsume short-lived previous revisions
        //  - Filter object contents
        objectHistory = objectHistory.appendRevision(revision);

        // Because we have only ever added information, it is safe to update
        // the histories map first; either this will merely provide the new
        // revision to an in-progress query, or the object will not yet be in
        // the indices and so not visible to in-progress queries.
        histories = histories.put(objectKey, objectHistory);

        // Now that the new history is in place, the tree index may be safely
        // updated if a new object was created.
        tree = nextTree;
    }

    public IntervalTree<IP, ObjectKey, IpInterval> getTree() {
        return tree;
    }

    public Optional<ObjectHistory> getObjectHistory(ObjectKey objectKey) {
        return Optional.ofNullable(histories.get(objectKey));
    }

    /* ---------------------------------------------------------------------- */
    /* Boring bits below.  Serialization via Externalizable */
    /* ---------------------------------------------------------------------- */

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(serial);
        Object[] thePast = histories.toArray();
        out.writeInt(thePast.length);
        for (Object obj : thePast) {
            @SuppressWarnings("unchecked")
            Pair<ObjectKey, ObjectHistory> p = (Pair<ObjectKey, ObjectHistory>)obj;
            out.writeObject(p.component1());
            out.writeObject(p.component2());
        }
        out.writeObject(tree);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serial = in.readLong();
        Builder<Pair<ObjectKey,ObjectHistory>,Map<ObjectKey,ObjectHistory>> builder = Maps.builder();
        int l = in.readInt();
        for (int i = 0; i < l; i++) {
            builder.add(new Pair<>((ObjectKey)in.readObject(), (ObjectHistory)in.readObject()));
        }
        histories = builder.build();
        tree = (AvlTree<IP, ObjectKey, IpInterval>)in.readObject();
    }
}
