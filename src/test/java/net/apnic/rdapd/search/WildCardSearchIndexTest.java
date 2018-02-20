package net.apnic.rdapd.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.ObjectSearchKey;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.rdap.RdapObject;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class WildCardSearchIndexTest
{
    @Test
    public void checkExactMatching()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY,"bat1")),
            revision(new ObjectKey(ObjectClass.ENTITY,"bat12")),
            revision(new ObjectKey(ObjectClass.ENTITY,"bat")),
            revision(new ObjectKey(ObjectClass.ENTITY,"wing-bat1"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        // Exact seach for bat1
        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat1");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 1);
        assertEquals(keys.get(0).getObjectName(), "bat1");

        // Exact seach for bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "bat12");
        keys = index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 1);
        assertEquals(keys.get(0).getObjectName(), "bat12");
    }

    @Test
    public void checkWildCardMatching()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY,"bat1")),
            revision(new ObjectKey(ObjectClass.ENTITY,"bat12")),
            revision(new ObjectKey(ObjectClass.ENTITY,"bat")),
            revision(new ObjectKey(ObjectClass.ENTITY,"wing-bat1"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        // Wild card seach for bat1, bat12
        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat1*");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 2);

        // Wild card seach for bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "bat*");
        keys = index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 3);

        // Wild card seach for wing-bat1, bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "*bat*");
        keys = index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 4);
        //
        // Wild card seach for wing-bat1, bat, bat1, bat12
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "*b*t*");
        keys = index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 4);

        // Wild card seach for wing-bat1
        sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle", "wing*");
        keys = index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 1);
    }

    @Test
    public void checkIndexDuplicateInsertion()
    {
        List<Revision> revisions = Arrays.asList(
            revision(new ObjectKey(ObjectClass.ENTITY, "bat12")),
            revision(new ObjectKey(ObjectClass.ENTITY, "bat12"))
        );

        WildCardSearchIndex index = new WildCardSearchIndex(
            ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        revisions.forEach(rev -> index.putMapping(rev, rev.getContents().getObjectKey()));

        ObjectSearchKey sKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
            "bat12");
        List<ObjectKey> keys =
            index.getObjectsForKey(sKey, 10).getKeys().collect(Collectors.toList());
        assertEquals(keys.size(), 1);
    }

    @Test
    public void foreignClassesAreNotIndexed() {
        WildCardSearchIndex index = new WildCardSearchIndex(
                ObjectClass.ENTITY, "handle",
                (rev, objectKey) -> Stream.of(objectKey.getObjectName()));

        ObjectKey objectKey = new ObjectKey(ObjectClass.AUT_NUM, "myObject");
        index.putMapping(revision(objectKey), objectKey);

        List<ObjectKey> results = index.getObjectsForKey(new ObjectSearchKey(ObjectClass.ENTITY, "handle", "myObject"), 10)
                .getKeys().collect(Collectors.toList());

        assertThat(results, empty());
    }

    private Revision revision(ObjectKey objectKey) {
        return new Revision(null, null,
                new EmptyObject(objectKey));
    }

    static class EmptyObject implements RdapObject {
        private final ObjectKey key;

        public EmptyObject(ObjectKey key) {
            this.key = key;
        }

        @Override
        public ObjectKey getObjectKey() {
            return key;
        }

        @Override
        public boolean isDeleted() {
            return false;
        }
    }
}
