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

    public ObjectHistory appendRevision(Revision revision) {
        return new ObjectHistory(objectKey, revisions.append(revision));
    }

    /**
     * Construct a new ObjectHistory taking related objects into account.
     *
     * For each revision, each related object of that revision is checked for
     * variations in the revision's valid lifetime, and the revision is split
     * around those changes.
     *
     * @param relatedObjects A mapping from ObjectKey to some ObjectHistory
     * @return A new ObjectHistory with related objects included
     */
    public ObjectHistory withRelatedObjects(RelatedObjects relatedObjects) {
        // TODO: do, too
        return this;
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

        public Wrapper(ObjectKey objectKey, List<Revision> revisions) {
            this.objectKey = objectKey;
            this.revisions = revisions.toArray(new Revision[0]);
        }

        private Object readResolve() {
            return new ObjectHistory(objectKey, IndexedLists.copyOf(revisions));
        }
    }


    //    private static final Gson GSON = new Gson();
//    private static final long serialVersionUID = 1;
//
//    private final transient @Nullable String rawJSON;
//    private final List<RpslRecord> records;
//
//    private ObjectHistory(List<RpslRecord> records, Optional<String> rawJSON) {
//        if (records.isEmpty()) {
//            throw new IllegalArgumentException("The list of records must not be empty");
//        }
//        this.rawJSON = rawJSON.orElse(null);
//        this.records = records;
//    }
//
//    /**
//     * Construct a History from a stream of records.
//     *
//     * @param records the records to combine into the new History
//     * @return a stream of one History if at least one record exists, else an empty stream
//     */
//    public static Stream<ObjectHistory> fromStream(Stream<RpslRecord> records) {
//        List<RpslRecord> rs = records.sorted().collect(Collectors.toList());
//        return rs.isEmpty() ? Stream.empty() : Stream.of(new ObjectHistory(rs, Optional.empty()));
//    }
//
//    /**
//     * Return a new History with a new update appended.
//     *
//     * The most recent RpslRecord will be truncated at the whence of the given update.  Any cached JSON state
//     * will be lost.
//     *
//     * @param update The new RpslRecord to append
//     * @return A new History
//     */
//    public ObjectHistory appendUpdate(RpslRecord update) {
//        // TODO: probably need to sort this, now
//        List<RpslRecord> newRecords = new ArrayList<>(records.size() + 1);
//        Optional<RpslRecord> last = records.isEmpty()
//                ? Optional.empty()
//                : Optional.of(records.get(records.size() - 1));
//
//        newRecords.addAll(records);
//        last.filter(r -> r.getUntil().isAfter(update.getWhence()))
//                .ifPresent(r -> newRecords.set(records.size() - 1, r.splitAt(update.getWhence())[0]));
//        newRecords.add(update);
//
//        return new ObjectHistory(newRecords, Optional.empty());
//    }
//
//    /**
//     * Update a History by mapping each record in turn to a stream of new records.
//     *
//     * @param fn the function to map a record to a stream of records
//     * @return a new History if there is at least one resulting record, or empty otherwise
//     */
//    public Optional<ObjectHistory> flatMap(Function<RpslRecord, Stream<RpslRecord>> fn) {
//        List<RpslRecord> rs = records.stream().sequential().flatMap(fn).sorted().collect(Collectors.toList());
//
//        if (rs.isEmpty()) {
//            return Optional.empty();
//        } else {
//            return Optional.of(new ObjectHistory(rs, Optional.empty()));
//        }
//    }
//
//    /**
//     * Construct a new History where the JSON representation is precomputed.
//     *
//     * @return A new History where the JSON representation is precomputed
//     */
//    public ObjectHistory cached() {
//        return new ObjectHistory(records, Optional.of(makeJson()));
//    }
//
//    public String getPrimaryKey() {
//        return records.get(0).getPrimaryKey();
//    }
//
//    public List<RpslRecord> getRecords() {
//        return records;
//    }
//
//    @JsonValue
//    @JsonRawValue
//    /**
//     * Return the JSON for this History.  May have been precomputed if you used .cached().
//     *
//     * This will not cache the JSON for future calls, as History cannot be changed.
//     *
//     * @return the JSON for this History
//     */
//    public String toJson() {
//        return rawJSON != null ? rawJSON : makeJson();
//    }
//
//    private JsonArray streamToJson(Stream<JsonElement> contents) {
//        final JsonArray json = new JsonArray();
//        contents.sequential().forEach(json::add);
//        return json;
//    }
//
//    private String makeJson() {
//        JsonObject rdap = new JsonObject();
//
//        // TODO: specialize this class for v4/v6/ASN
//        rdap.addProperty("objectClassName", "ip network history");
//        rdap.addProperty("handle", records.get(0).getPrimaryKey());
//        rdap.addProperty("ipVersion", records.get(0).getObjectType() == 6 ? "v4" : "v6");
//        rdap.add("versions", streamToJson(records.stream().map(this::rpslToRdap)));
//
//        return GSON.toJson(rdap);
//    }
//
//    private JsonObject rpslToRdap(RpslRecord rpsl) {
//        JsonObject rdap = new JsonObject();
//
//        Optional<RpslObject> base = rpsl.getRpslObject();
//
//        rdap.add("applicability", streamToJson(Stream.of(rpsl.getWhence(), rpsl.getUntil())
//                .map(Object::toString).map(JsonPrimitive::new)));
//        attrToProperty(base, rdap, AttributeType.NETNAME, "name");
//        attrToProperty(base, rdap, AttributeType.STATUS, "type");
//        attrToProperty(base, rdap, AttributeType.COUNTRY, "country");   // note: more than one country means NO entry!
//        addRemarks(base, rdap);
//        addRaw(rpsl, rdap);
//
//        return rdap;
//    }
//
//    private void attrToProperty(Optional<RpslObject> object, JsonObject json, AttributeType attribute, String property) {
//        object.map(o -> o.getValueOrNullForAttribute(attribute))
//                .ifPresent(v -> json.addProperty(property, v.toString()));
//    }
//
//    private void addRemarks(Optional<RpslObject> object, JsonObject json) {
//        JsonArray remarks = new JsonArray();
//
//        object.ifPresent(o -> {
//            List<RpslAttribute> descrs = o.findAttributes(AttributeType.DESCR);
//            if (!descrs.isEmpty()) {
//                JsonObject remark = new JsonObject();
//                remark.addProperty("title", "description");
//                JsonArray labels = new JsonArray();
//                descrs.forEach(a -> labels.add(a.getCleanValue().toString()));
//                remark.add("description", labels);
//                remarks.add(remark);
//            }
//
//            List<RpslAttribute> rems = o.findAttributes(AttributeType.REMARKS);
//            if (!rems.isEmpty()) {
//                JsonObject remark = new JsonObject();
//                remark.addProperty("title", "remarks");
//                JsonArray labels = new JsonArray();
//                rems.forEach(a -> labels.add(a.getCleanValue().toString()));
//                remark.add("description", labels);
//                remarks.add(remark);
//            }
//
//            json.add("remarks", remarks);
//        });
//    }
//
//    private String whitespaceNormalised(RpslObject object) {
//        return object.toString();
//    }
//
//    private void addRaw(RpslRecord record, JsonObject json) {
//        JsonArray raw = new JsonArray();
//
//        raw.add(record.getRpslObject().map(this::whitespaceNormalised).get());
//        record.getChildren().stream()
//                .map(RpslRecord::getRpslObject)
//                .sorted(Comparator.comparing(k -> k.map(RpslObject::getKey).orElse(CIString.ciString(""))))
//                .forEach(k -> k.ifPresent(o -> raw.add(whitespaceNormalised(o))));
//
//        json.add("rpsl", raw);
//    }
//
//    private Object readResolve() throws ObjectStreamException {
//        return new ObjectHistory(records, Optional.empty());
//    }
}
