package net.apnic.whowas.search;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;
import net.apnic.whowas.search.WildCardSearchIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class WildCardSearchIndexTest
{
    @Test
    public void checkExactMatching()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY, "bat1"),
                     "person: Batman\nhandle:bat1\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "bat12"),
                     "person: Batman12\nhandle:bat12\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "bat"),
                     "person: Batman\nhandle:bat\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "wing-bat1"),
                     "person: Batman\nhandle:bat\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        // Exact seach for bat1
        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat1");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 1);
        assertEquals(keys.get(0).getObjectName(), "bat1");

        // Exact seach for bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "bat12");
        keys = index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 1);
        assertEquals(keys.get(0).getObjectName(), "bat12");
    }

    @Test
    public void checkWildCardMatching()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY, "bat1"),
                     "person: Batman\nhandle:bat1\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "bat12"),
                     "person: Batman12\nhandle:bat12\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "bat"),
                     "person: Batman\nhandle:bat\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "wing-bat1"),
                     "person: Batman\nhandle:bat\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        // Wild card seach for bat1, bat12
        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat1*");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 2);

        // Wild card seach for bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "bat*");
        keys = index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 3);

        // Wild card seach for wing-bat1, bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "*bat*");
        keys = index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 4);
        //
        // Wild card seach for wing-bat1, bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "*b*t*");
        keys = index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 4);

        // Wild card seach for wing-bat1
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "wing*");
        keys = index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 1);
    }

    @Test
    public void checkIndexDuplicateInsertion()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY, "bat12"),
                     "person: Batman\nhandle:bat1\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00")),

            revision(new ObjectKey(ObjectClass.ENTITY, "bat12"),
                     "person: Batman12\nhandle:bat12\n".getBytes(),
                     ZonedDateTime.parse("2017-01-01T00:00:00.000+10:00"),
                     ZonedDateTime.parse("2017-02-01T00:00:00.000+10:00"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat12");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).collect(Collectors.toList());
        assertEquals(keys.size(), 1);
    }

    private Revision revision(
        ObjectKey objectKey,
        byte[] rpsl,
        ZonedDateTime validFrom,
        ZonedDateTime validUntil)
    {
        return new Revision(validFrom, validUntil,
            RpslToRdap.rpslToRdap(objectKey, rpsl));
    }
}
