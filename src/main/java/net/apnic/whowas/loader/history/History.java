package net.apnic.whowas.loader.history;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.*;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A history for a particular INR interval.
 *
 * @author bje
 */
public final class History {
    private static final Gson GSON = new Gson();

    private final Optional<String> rawJSON;
    private final List<RpslRecord> records;

    private History(List<RpslRecord> records, Optional<String> rawJSON) {
        if (records.isEmpty()) {
            throw new IllegalArgumentException("The list of records must not be empty");
        }
        this.rawJSON = rawJSON;
        this.records = records;
    }

    /**
     * Construct a History from a stream of records.
     *
     * @param records the records to combine into the new History
     * @return a stream of one History if at least one record exists, else an empty stream
     */
    public static Stream<History> fromStream(Stream<RpslRecord> records) {
        List<RpslRecord> rs = records.sorted().collect(Collectors.toList());
        return rs.isEmpty() ? Stream.empty() : Stream.of(new History(rs, Optional.empty()));
    }

    /**
     * Return a new History with a new update appended.
     *
     * The most recent RpslRecord will be truncated at the whence of the given update.  Any cached JSON state
     * will be lost.
     *
     * @param update The new RpslRecord to append
     * @return A new History
     */
    public History appendUpdate(RpslRecord update) {
        // TODO: probably need to sort this, now
        List<RpslRecord> newRecords = new ArrayList<>(records.size() + 1);
        Optional<RpslRecord> last = records.isEmpty()
                ? Optional.empty()
                : Optional.of(records.get(records.size() - 1));

        newRecords.addAll(records);
        last.filter(r -> r.getUntil().isAfter(update.getWhence()))
                .ifPresent(r -> newRecords.set(records.size() - 1, r.splitAt(update.getWhence())[0]));
        newRecords.add(update);

        return new History(newRecords, Optional.empty());
    }

    /**
     * Update a History by mapping each record in turn to a stream of new records.
     *
     * @param fn the function to map a record to a stream of records
     * @return a new History if there is at least one resulting record, or empty otherwise
     */
    public Optional<History> flatMap(Function<RpslRecord, Stream<RpslRecord>> fn) {
        List<RpslRecord> rs = records.stream().sequential().flatMap(fn).sorted().collect(Collectors.toList());

        if (rs.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new History(rs, Optional.empty()));
        }
    }

    /**
     * Construct a new History where the JSON representation is precomputed.
     *
     * @return A new History where the JSON representation is precomputed
     */
    public History cached() {
        return new History(records, Optional.of(makeJson()));
    }

    public String getPrimaryKey() {
        return records.get(0).getPrimaryKey();
    }

    @JsonValue
    @JsonRawValue
    /**
     * Return the JSON for this History.  May have been precomputed if you used .cached().
     *
     * This will not cache the JSON for future calls, as History cannot be changed.
     *
     * @return the JSON for this History
     */
    public String toJson() {
        return rawJSON.orElse(makeJson());
    }

    private JsonArray streamToJson(Stream<JsonElement> contents) {
        final JsonArray json = new JsonArray();
        contents.forEach(json::add);
        return json;
    }

    private String makeJson() {
        JsonObject rdap = new JsonObject();

        // TODO: specialize this class for v4/v6/ASN
        rdap.addProperty("objectClassName", "ip network history");
        rdap.addProperty("handle", records.get(0).getPrimaryKey());
        rdap.addProperty("ipVersion", records.get(0).getObjectType() == 6 ? "v4" : "v6");
        rdap.add("versions", streamToJson(records.stream().map(this::rpslToRdap)));

        return GSON.toJson(rdap);
    }

    private JsonObject rpslToRdap(RpslRecord rpsl) {
        JsonObject rdap = new JsonObject();

        Optional<RpslObject> base = rpsl.getRpslObject();

        rdap.add("applicability", streamToJson(Stream.of(rpsl.getWhence(), rpsl.getUntil())
                .map(Object::toString).map(JsonPrimitive::new)));
        attrToProperty(base, rdap, AttributeType.NETNAME, "name");
        attrToProperty(base, rdap, AttributeType.STATUS, "type");
        attrToProperty(base, rdap, AttributeType.COUNTRY, "country");   // note: more than one country means NO entry!
        addRemarks(base, rdap);
        addRelated(rpsl, rdap);
        addRaw(rpsl, rdap);

        return rdap;
    }

    private void attrToProperty(Optional<RpslObject> object, JsonObject json, AttributeType attribute, String property) {
        object.map(o -> o.getValueOrNullForAttribute(attribute))
                .ifPresent(v -> json.addProperty(property, v.toString()));
    }

    private void addRemarks(Optional<RpslObject> object, JsonObject json) {
        JsonArray remarks = new JsonArray();

        object.ifPresent(o -> {
            List<RpslAttribute> descrs = o.findAttributes(AttributeType.DESCR);
            if (!descrs.isEmpty()) {
                JsonObject remark = new JsonObject();
                remark.addProperty("title", "description");
                JsonArray labels = new JsonArray();
                descrs.forEach(a -> labels.add(a.getCleanValue().toString()));
                remark.add("description", labels);
                remarks.add(remark);
            }

            List<RpslAttribute> rems = o.findAttributes(AttributeType.REMARKS);
            if (!rems.isEmpty()) {
                JsonObject remark = new JsonObject();
                remark.addProperty("title", "remarks");
                JsonArray labels = new JsonArray();
                rems.forEach(a -> labels.add(a.getCleanValue().toString()));
                remark.add("description", labels);
                remarks.add(remark);
            }

            json.add("remarks", remarks);
        });
    }

    private void addRaw(RpslRecord record, JsonObject json) {
        JsonArray raw = new JsonArray();

        raw.add(record.getRaw());
        record.getChildren().forEach(k -> raw.add(k.getRaw()));

        json.add("rpsl", raw);
    }

    private void addRelated(RpslRecord parent, JsonObject json) {
        JsonArray entities = new JsonArray();

        for (RpslRecord child : parent.getChildren()) {

        }
    }
}