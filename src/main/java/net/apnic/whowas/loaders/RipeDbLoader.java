package net.apnic.whowas.loaders;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.GenericObject;
import net.apnic.whowas.types.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
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

    private static void resultSetToRdap(ResultSet rs, RevisionConsumer consumer)
        throws SQLException {
        ObjectClass objectClass = OBJECT_CLASSES.getOrDefault(rs.getInt("object_type"), null);

        if (objectClass != null) {
            byte[] contents = rs.getBytes("object");
            ObjectKey objectKey = new ObjectKey(objectClass, rs.getString("pkey"));
            consumer.accept(objectKey, new Revision(
                    fromStamp(rs.getLong("timestamp")),
                    null,
                    new GenericObject(objectKey, contents)));
        }
        else
        {
            LOGGER.warn("Unknown object type detected " + rs.getInt("object_type"));
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public void loadWith(RevisionConsumer consumer) {
        final String query = lastSerial > 0 ? RipeDbLoaderUtil.LOAD_QUERY_WITH_SERIAL
                                            : RipeDbLoaderUtil.LOAD_QUERY_WITHOUT_SERIAL;
        final Object[] args = lastSerial > 0 ? new Object[] { lastSerial, lastSerial }
                                             : new Object[0];

        operations.query(
            c -> {
                PreparedStatement stmt = c.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                try {
                    stmt.setFetchSize(Integer.MIN_VALUE);
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i+1, args[i]);
                    }
                } catch (SQLException ex) {
                    stmt.close();
                    throw ex;
                }
                return stmt;
            },
            (ResultSet rs) -> resultSetToRdap(rs, consumer));

        operations.query(RipeDbLoaderUtil.SERIAL_MAX, (ResultSet rs) -> {
            long nextSerial = rs.getLong("serial");
            if (nextSerial > lastSerial) {
                LOGGER.info("Data refreshed up to serial {}", nextSerial);
                lastSerial = nextSerial;
            }
        });
        LOGGER.debug("All database records loaded");
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
            new Tuple<>(2, ObjectClass.AUT_NUM),
            new Tuple<>(3, ObjectClass.DOMAIN),
            new Tuple<>(5, ObjectClass.IP_NETWORK),
            new Tuple<>(6, ObjectClass.IP_NETWORK),
            new Tuple<>(9, ObjectClass.ENTITY),
            new Tuple<>(10, ObjectClass.ENTITY),
            new Tuple<>(11, ObjectClass.ENTITY),
            new Tuple<>(17, ObjectClass.ENTITY),
            new Tuple<>(18, ObjectClass.ENTITY))
        .collect(Collectors.toMap(Tuple::fst, Tuple::snd));
}
