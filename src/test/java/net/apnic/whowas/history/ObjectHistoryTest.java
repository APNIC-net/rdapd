package net.apnic.whowas.history;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Stream;

public class ObjectHistoryTest {

    RpslRecord makeRPSL(int sequence, String key, String raw) {
        return new RpslRecord(
                42, key, key.hashCode(), sequence,
                LocalDateTime.of(2016, 1, sequence, 1, 1, 1),
                LocalDateTime.of(2016, 1, sequence, 14, 1, 1),
                raw, Collections.emptyList()
        );
    }

    @Test
    public void testNormalisation() throws Exception {
        final Stream<RpslRecord> records = Stream.of(
                makeRPSL(1, "TEST-OBJECT",
                        "person: Testy McTestFace\nnic-hdl: TEST-OBJECT\nkey: value\n  value\n+         valymcvalue\n")
        );
        final ObjectHistory history = ObjectHistory.fromStream(records).findFirst().get();
    }
}