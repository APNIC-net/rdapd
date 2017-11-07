package net.apnic.whowas.history;

import com.github.andrewoma.dexx.collection.*;

import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.intervaltree.avl.AvlTree;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The History of a registry.
 *
 * The history of a registry is the history of each object within the registry,
 * a serial number reflecting the version of the registry, and a set of indices
 * for fast interval lookups.
 */
public final class History implements Externalizable, ObjectIndex {
    private static final long serialVersionUID = 5063296486972345480L;
    private static final Logger LOGGER = LoggerFactory.getLogger(History.class);

    /* The history of every object */
    private volatile Map<ObjectKey, ObjectHistory> histories;

    /* IP interval index */
    private volatile AvlTree<IP, ObjectKey, IpInterval> tree;

    /* Related object index */
    private volatile Map<ObjectKey, Set<ObjectKey>> relatedIndex;

    /**
     * Construct a new History in which nothing has ever happened.
     */
    public History() {
        histories = HashMap.empty();
        tree = new AvlTree<>();
        relatedIndex = HashMap.empty();
    }

    /**
     * Horrible way to deserialize a History.
     *
     * App uses a final instance variable for its history.
     *
     * @param history the shameful history of past of software design choices
     */
    public synchronized void deserialize(History history) {
        this.histories = history.histories;
        this.tree = history.tree;
        this.relatedIndex = history.relatedIndex;
    }

    /**
     * Update an object in the History with a new revision.
     *
     * Revisions MUST be added in chronological order.  This code assumes that
     * each revision is always later than or simultaneous to the last revision
     * added.
     *
     * This method mutates the History. Be careful not to serialize the History
     * while this operation is in progress, as consistency is only guaranteed
     * at the end of the method.
     *
     * Read consistency of the tree and object histories will remain valid
     * during operation.  Any tree or history obtained previously will not see
     * the updates, as both structures are replaced in this method.
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
            if (objectKey.getObjectClass() == ObjectClass.IP_NETWORK) {
                nextTree = updateIntervalTree(objectKey, nextTree);
            }
        }

        // Handy information about this object's history and latest revision
        Optional<Revision> mostRecent = objectHistory.mostRecent();
        Collection<ObjectKey> entityKeys = revision.getContents().getEntityKeys();

        // Add any related entities to the revision's content
        revision = addRelatedObjects(revision);

        // Ensure that the revision has actually changed: some WHOIS attributes
        // have no bearing on the RDAP object structure, and spurious changes
        // should be suppressed.
        // TODO do

        // The related index is owned exclusively by this method (and its
        // private method components).  Because this method is synchronized,
        // the index can be updated safely without regard to ordering of updates
        // to the tree or object history set.

        // Update the related index to track objects referencing entities
        updateRelatedIndex(objectKey, entityKeys, mostRecent.orElse(null));

        // Link it on in
        objectHistory = objectHistory.appendRevision(revision);

        // Because we have only ever added information, it is safe to update
        // the histories map first; either this will merely provide the new
        // revision to an in-progress query, or the object will not yet be in
        // the indices and so not visible to in-progress queries.
        histories = histories.put(objectKey, objectHistory);

        // Check the related index to see if this object is related to anything
        updateRelatingObjects(objectKey, revision);

        // Now that the new history is in place, the tree index may be safely
        // updated if a new object was created.
        tree = nextTree;
    }

    /* Find any objects which relate to this object, and add a new revision */
    private void updateRelatingObjects(ObjectKey objectKey, Revision revision) {
        Set<ObjectKey> relations = Optional.ofNullable(relatedIndex.get(objectKey))
                .orElse(HashSet.empty());
        for (ObjectKey key : relations) {
            ObjectHistory relatedHistory = histories.get(key);
            final Revision lambdasAreNotClosures = revision;
            Optional.ofNullable(relatedHistory)
                    .flatMap(ObjectHistory::mostRecent)
                    .map(r -> new Revision(
                            lambdasAreNotClosures.getValidFrom(),
                            null,
                            r.getContents()))
                    .map(this::addRelatedObjects)
                    .ifPresent(r -> {
                        assert relatedHistory != null;
                        histories = histories.put(key, relatedHistory.appendRevision(r));
                    });
        }
    }

    /* Update the interval tree to reflect a new IP network object revision */
    private AvlTree<IP, ObjectKey, IpInterval> updateIntervalTree(ObjectKey objectKey, AvlTree<IP, ObjectKey, IpInterval> nextTree) {
        try {
            IpInterval interval = Parsing.parseInterval(objectKey.getObjectName());
            nextTree = tree.update(interval, objectKey, (a,b) -> {
                assert a.equals(b);
                return a;
            }, o -> o);
        } catch (Exception ex) {
            LOGGER.warn("Object {} not added to IP tree: parse exception {}", objectKey, ex.getMessage());
            LOGGER.debug("Full exception", ex);
            // absorb and move on
        }
        return nextTree;
    }

    /* Maintain the index of related objects */
    private void updateRelatedIndex(ObjectKey objectKey, Collection<ObjectKey> entityKeys, Revision mostRecent) {
        Set<ObjectKey> relatedKeys = Sets.copyOf(Optional.ofNullable(mostRecent)
                .map(Revision::getContents)
                .map(RdapObject::getEntityKeys)
                .map(Collection::iterator)
                .orElse(Collections.emptyIterator()));

        // Remove any links no longer required
        Set<ObjectKey> removeKeys = relatedKeys;
        for (ObjectKey key : entityKeys) {
            removeKeys = removeKeys.remove(key);
        }
        for (ObjectKey key : removeKeys) {
            Set<ObjectKey> keys = Optional.ofNullable(relatedIndex.get(key))
                    .orElse(HashSet.empty())
                    .remove(objectKey);
            if (keys.isEmpty()) {
                relatedIndex = relatedIndex.remove(key);
            } else {
                relatedIndex = relatedIndex.put(key, keys);
            }
        }

        // Add any new links
        Set<ObjectKey> newKeys = Sets.copyOf(entityKeys);
        for (ObjectKey key : relatedKeys) {
            newKeys = newKeys.remove(key);
        }
        for (ObjectKey key : newKeys) {
            Set<ObjectKey> keys = Optional.ofNullable(relatedIndex.get(key))
                    .orElse(HashSet.empty())
                    .add(objectKey);
            relatedIndex = relatedIndex.put(key, keys);
        }
    }

    /* Add related objects from the history to the given revision */
    private Revision addRelatedObjects(Revision revision) {
        return new Revision(revision.getValidFrom(), revision.getValidUntil(),
                revision.getContents().withEntities(
                        revision.getContents().getEntityKeys().stream()
                                .map(histories::get)
                                .filter(Objects::nonNull)
                                .map(ObjectHistory::mostRecent)
                                .flatMap(o -> o.map(Stream::of).orElse(Stream.empty()))
                                .map(Revision::getContents)
                                .collect(Collectors.toList())));
    }

    public IntervalTree<IP, ObjectKey, IpInterval> getTree() {
        return tree;
    }

    @Override
    public Optional<ObjectHistory> historyForObject(ObjectKey objectKey) {
        return Optional.ofNullable(histories.get(objectKey));
    }

    @Override
    public Stream<ObjectHistory> historyForObject(Stream<ObjectKey> objectKeys)
    {
        return objectKeys.map(histories::get)
            .filter(x -> x != null);
    }

    /* ---------------------------------------------------------------------- */
    /* Boring bits below.  Serialization via Externalizable */
    /* ---------------------------------------------------------------------- */

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Object[] thePast = histories.toArray();
        out.writeInt(thePast.length);
        for (Object obj : thePast) {
            @SuppressWarnings("unchecked")
            Pair<ObjectKey, ObjectHistory> p = (Pair<ObjectKey, ObjectHistory>)obj;
            out.writeObject(p.component1());
            out.writeObject(p.component2());
        }
        Object[] links = relatedIndex.toArray();
        ObjectKey[] keys = {};
        out.writeInt(links.length);
        for (Object obj : links) {
            @SuppressWarnings("unchecked")
            Pair<ObjectKey, Set<ObjectKey>> p = (Pair<ObjectKey, Set<ObjectKey>>)obj;
            out.writeObject(p.component1());
            out.writeObject(p.component2().toArray(keys));
        }
        out.writeObject(tree);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Builder<Pair<ObjectKey,ObjectHistory>,Map<ObjectKey,ObjectHistory>> builder = Maps.builder();
        int l = in.readInt();
        for (int i = 0; i < l; i++) {
            builder.add(new Pair<>((ObjectKey)in.readObject(), (ObjectHistory)in.readObject()));
        }
        histories = builder.build();
        Builder<Pair<ObjectKey, Set<ObjectKey>>,Map<ObjectKey, Set<ObjectKey>>> rBuilder = Maps.builder();
        l = in.readInt();
        for (int i = 0; i < l; i++) {
            rBuilder.add(new Pair<>(
                    (ObjectKey)in.readObject(),
                    Sets.copyOf((ObjectKey[])in.readObject())));
        }
        relatedIndex = rBuilder.build();
        tree = (AvlTree<IP, ObjectKey, IpInterval>)in.readObject();
    }
}
