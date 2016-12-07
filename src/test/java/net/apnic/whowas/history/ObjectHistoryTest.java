package net.apnic.whowas.history;

import net.apnic.whowas.rdap.RdapObject;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ObjectHistoryTest {
    private static final ObjectKey objectKey = new ObjectKey(ObjectClass.ENTITY, "DUMMY");

    RdapObject dummy = new RdapObject() {
        @Override
        public ObjectKey getObjectKey() {
            return objectKey;
        }

        @Override
        public Collection<ObjectKey> getEntityKeys() {
            return Collections.emptyList();
        }

        @Override
        public RdapObject withEntities(Collection<RdapObject> relatedEntities) {
            return this;
        }
    };

    @Test
    public void testSquelch() {
        ObjectHistory objectHistory = new ObjectHistory(objectKey);
        ZonedDateTime stamp = ZonedDateTime.of(2016, 12, 7, 11, 28, 33, 761, ZoneId.systemDefault());
        Revision[] revisions = new Revision[] {
                new Revision(stamp, null, dummy),
                new Revision(stamp.plusSeconds(3), null, dummy)
        };
        objectHistory = objectHistory.appendRevision(revisions[0]);
        objectHistory = objectHistory.appendRevision(revisions[1]);
        assertThat("A revision should be squelched", objectHistory, contains(revisions[1]));
        assertThat("The other revision is gone", objectHistory, not(contains(revisions[0])));
    }
}