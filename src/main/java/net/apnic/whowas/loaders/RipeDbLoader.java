package net.apnic.whowas.loaders;

import net.apnic.whowas.rdap.RdapEntity;
import net.apnic.whowas.rdap.IpNetwork;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.types.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RipeDbLoader implements Loader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RipeDbLoader.class);

    private final JdbcOperations operations;

    public RipeDbLoader(JdbcOperations jdbcOperations) {
        this.operations = jdbcOperations;
    }

    private static void resultSetToRdap(ResultSet rs, RevisionConsumer consumer) throws SQLException {
        ObjectClass objectClass = OBJECT_CLASSES.getOrDefault(rs.getInt("object_type"), null);
        if (objectClass != null) {
            RdapObject rdapObject;
            byte[] contents = rs.getBytes("object");
            ObjectKey objectKey = new ObjectKey(objectClass, rs.getString("pkey"));

            try {
                switch (objectClass) {
                    case IP_NETWORK:
                        rdapObject = contents.length == 0
                                ? IpNetwork.deletedObject(objectKey)
                                : new IpNetwork(objectKey, contents);
                        break;
                    case DOMAIN:
                        rdapObject = null;
                        break;
                    case AUT_NUM:
                        rdapObject = null;
                        break;
                    case ENTITY:
                        rdapObject = contents.length == 0
                                ? RdapEntity.deletedObject(objectKey)
                                : new RdapEntity(objectKey, contents);
                        break;
                    default:
                        throw new IllegalArgumentException("An imaginary object class exists");
                }
                consumer.accept(
                        new ObjectKey(objectClass, rs.getString("pkey")),
                        new Revision(
                                fromStamp(rs.getLong("timestamp")),
                                null,
                                rdapObject));
            } catch (Exception ex) {
                LOGGER.error("Cannot load object {}: {}", rs.getString("pkey"), ex.getLocalizedMessage());
                LOGGER.debug("Full exception was", ex);
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public void loadWith(RevisionConsumer consumer) {
        Function<String, String> query =
                tbl -> "SELECT object_id, object_type, pkey, sequence_id, timestamp, object FROM " + tbl + " " +
                        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18) " +
                        "AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'NO4-AP') " +
                        "ORDER BY timestamp, object_id, sequence_id";

        operations.query(query.apply("last, history"), (ResultSet rs) -> resultSetToRdap(rs, consumer));
//        Stream.of("history", "last").forEach(t -> {
//            LOGGER.info("Executing database queries for {} table", t);
//            operations.query(query.apply(t), (ResultSet rs) -> resultSetToRdap(rs, consumer));
//        });
        LOGGER.info("All database records loaded");
    }

    private static ZonedDateTime fromStamp(long stamp) {
        return Instant.ofEpochSecond(stamp).atZone(ZoneId.systemDefault());
    }

    // Presence in the map serves as a proxy for relevance to this application
    private static final Map<Integer, ObjectClass> OBJECT_CLASSES = Stream.of(
            new Tuple<>(3, ObjectClass.DOMAIN),
            new Tuple<>(6, ObjectClass.IP_NETWORK),
            new Tuple<>(9, ObjectClass.ENTITY),
            new Tuple<>(10, ObjectClass.ENTITY),
            new Tuple<>(11, ObjectClass.ENTITY),
            new Tuple<>(18, ObjectClass.ENTITY),
            new Tuple<>(5, ObjectClass.IP_NETWORK),
            new Tuple<>(0, ObjectClass.AUT_NUM),
            new Tuple<>(2, ObjectClass.AUT_NUM),
            new Tuple<>(17, ObjectClass.ENTITY)
    ).collect(Collectors.toMap(Tuple::fst, Tuple::snd));
}
