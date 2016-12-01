package net.apnic.whowas.loaders;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.IpNetwork;
import net.apnic.whowas.rdap.RdapEntity;
import net.apnic.whowas.rdap.RdapObject;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RipeDbLoader implements Loader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RipeDbLoader.class);

    private long lastSerial;
    private final transient JdbcOperations operations;

    public RipeDbLoader(JdbcOperations jdbcOperations, long serial) {
        this.lastSerial = serial;
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
        String QUERY =  "(SELECT object_id, object_type, pkey, sequence_id, timestamp, object FROM last\n" +
                        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                        "      AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER'))\n" +
//                        "      AND pkey IN ('JAY-SPIDER'))\n" +
                        "    UNION\n" +
                        "(SELECT object_id, object_type, pkey, sequence_id, timestamp, object FROM history\n" +
                        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                        "      AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER'))\n" +
//                        "      AND pkey IN ('JAY-SPIDER'))\n" +
                        "ORDER BY timestamp, object_id, sequence_id";
        Object[] arguments = {};

        if (lastSerial > 0) {
            QUERY = "(SELECT l.object_id, l.object_type, l.pkey, l.sequence_id, l.timestamp, l.object FROM last l, serials s\n" +
                    "WHERE l.object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                    "      AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER')\n" +
//                    "      AND pkey IN ('JAY-SPIDER')\n" +
                    "      AND l.object_id = s.object_id AND l.sequence_id = s.sequence_id AND s.serial_id > ?)\n" +
                    "    UNION\n" +
                    "(SELECT h.object_id, h.object_type, h.pkey, h.sequence_id, h.timestamp, h.object FROM history h, serials s\n" +
                    "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                    "      AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER')\n" +
//                    "      AND pkey IN ('JAY-SPIDER')\n" +
                    "      AND h.object_id = s.object_id AND h.sequence_id = s.sequence_id AND s.serial_id > ?)\n" +
                    "ORDER BY timestamp, object_id, sequence_id";
            arguments = new Object[] { lastSerial, lastSerial };
        }

        operations.query(QUERY, arguments, (ResultSet rs) -> resultSetToRdap(rs, consumer));
        operations.query("SELECT MAX(serial_id) AS `serial` FROM serials", (ResultSet rs) -> {
           LOGGER.info("Data refreshed up to serial {}", rs.getLong("serial"));
           lastSerial = rs.getLong("serial");
        });
        LOGGER.info("All database records loaded");
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public void loadChanges(RevisionConsumer revisionConsumer) {
        String QUERY =  "(SELECT l.object_id, l.object_type, l.pkey, l.sequence_id, l.timestamp, l.object FROM last l, serials s\n" +
                        "WHERE l.object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                        "      AND l.pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER')\n" +
                        "      AND l.object_id = s.object_id AND l.sequence_id = s.sequence_id AND s.serial_id >= ?)\n" +
                        "    UNION\n" +
                        "(SELECT h.object_id, h.object_type, h.pkey, h.sequence_id, h.timestamp, h.object FROM history h, serials s\n" +
                        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
                        "      AND pkey IN ('203.119.42.0 - 203.119.43.255', 'AIC1-AP', 'IRT-APNIC-IS-AP', 'IRT-APNIC-AP', 'NO4-AP', 'JAY-SPIDER')\n" +
                        "      AND h.object_id = s.object_id AND h.sequence_id = s.sequence_id AND s.serial_id >= ?)\n" +
                        "ORDER BY timestamp, object_id, sequence_id";
        Object[] arguments = { lastSerial, lastSerial };
        operations.query(QUERY, arguments, (ResultSet rs) -> resultSetToRdap(rs, (k,r) -> LOGGER.info("Catch up: {}", k)));
    }

    public long getLastSerial() {
        return lastSerial;
    }

    public void setLastSerial(long lastSerial) {
        this.lastSerial = lastSerial;
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
