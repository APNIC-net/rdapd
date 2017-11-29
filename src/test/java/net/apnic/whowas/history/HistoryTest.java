package net.apnic.whowas.history;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rdap.RelatedEntity;

public class HistoryTest {
    private static final ObjectKey DNS_KEY = new ObjectKey(ObjectClass.DOMAIN, "1.0.0.127.in-addr.arpa");
    private static final ObjectKey WHO_KEY = new ObjectKey(ObjectClass.ENTITY, "A-PERSON");

    private static final RdapObject DNS_OBJECT = new StaticObject(DNS_KEY,
            Collections.singleton(WHO_KEY));
    private static final RdapObject WHO_OBJECT = new StaticObject(WHO_KEY,
            Collections.emptyList());

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        History history = new History();
        history.addRevision(DNS_KEY, new Revision(
                ZonedDateTime.of(2016, 12, 6, 10, 10, 10, 0, ZoneId.systemDefault()),
                null, DNS_OBJECT));

        // Pass the History through a serialize/deserialize exchange
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(history);
        oos.close();
        history = (History) new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).readObject();

        // What went in should come out
        Optional<ObjectHistory> obj = history.historyForObject(DNS_KEY);
        assertTrue("History contains DNS key", obj.isPresent());
        assertTrue("The history has a revision",
                obj.flatMap(ObjectHistory::mostRecent).isPresent());
        assertThat("The revision's object hasn't been updated",
                obj.flatMap(ObjectHistory::mostRecent)
                        .map(Revision::getContents)
                        .filter((RdapObject o) -> o instanceof StaticObject)
                        .map(o -> ((StaticObject)o).getUpdatedTimes()),
                is(equalTo(Optional.of(0))));

        // And the linkages of related objects should have survived, too
        history.addRevision(WHO_KEY, new Revision(
                ZonedDateTime.of(2016, 12, 10, 10, 10, 10, 0, ZoneId.systemDefault()),
                null, WHO_OBJECT));
        obj = history.historyForObject(DNS_KEY);
        assertTrue("History still contains DNS key", obj.isPresent());
        assertThat("There's now two revisions",
                obj.map(o -> (Iterable<Revision>)o).orElse(Collections.emptyList()),
                is(iterableWithSize(2)));
        assertTrue("The history has a most recent revision",
                obj.flatMap(ObjectHistory::mostRecent).isPresent());
        assertThat("The revision's object has been updated",
                obj.flatMap(ObjectHistory::mostRecent)
                        .map(Revision::getContents)
                        .filter((RdapObject o) -> o instanceof StaticObject)
                        .map(o -> ((StaticObject)o).getUpdatedTimes()),
                is(equalTo(Optional.of(1))));
    }
}

class StaticObject implements Serializable, RdapObject {
    private ObjectKey objectKey;
    private Collection<ObjectKey> relatedObjects;
    private transient int updatedTimes = 0;

    StaticObject(ObjectKey objectKey, Collection<ObjectKey> relatedObjects) {
        this.objectKey = objectKey;
        this.relatedObjects = relatedObjects;
    }

    @Override
    public ObjectKey getObjectKey() {
        return objectKey;
    }

    @Override
    public Collection<ObjectKey> getEntityKeys() {
        return relatedObjects;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public RdapObject withRelatedEntities(
        Collection<RelatedEntity> relatedEntities) {
        updatedTimes++;
        return this;
    }

    int getUpdatedTimes() {
        return updatedTimes;
    }
}
