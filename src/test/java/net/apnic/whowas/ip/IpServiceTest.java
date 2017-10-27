package net.apnic.whowas.ip;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.history.config.HistoryConfiguration;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.types.Parsing;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IpServiceTest {

    ZonedDateTime dummyDateTime = null;

    @Test
    public void findsMostSpecificEncompassingRange() {
        History history = new History();
        for( String s : Arrays.asList(
                "10.0.0.0/8",
                "10.0.0.0/16",
                "10.0.0.0/22",
                "10.0.0.0/24",
                "10.0.1.0/24"
        )) {
            ObjectKey objectKey = new ObjectKey(ObjectClass.IP_NETWORK, s);
            history.addRevision(
                    objectKey,
                    new Revision(
                            dummyDateTime,
                            dummyDateTime,
                            new EmptyObject(objectKey)
                    )
            );
        }

        IpService ipService = new HistoryConfiguration().ipService(history);

        //Result is the most specific address range encompassing the search term
        assertThat(
                ipService.find(Parsing.parseCIDRInterval("10.0.0.0")).get().getObjectKey().getObjectName(),
                is("10.0.0.0/24")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("10.0.0.0/24")).get().getObjectKey().getObjectName(),
                is("10.0.0.0/24")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("10.0.0.0/23")).get().getObjectKey().getObjectName(),
                is("10.0.0.0/22")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("10.0.0.0/20")).get().getObjectKey().getObjectName(),
                is("10.0.0.0/16")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("0.0.0.0/0")),
                is(Optional.empty())
        );

    }

    @Test
    public void onlyCurrentRevisionsAreReturned() {
        History history = new History();
        {
            ObjectKey objectKey = new ObjectKey(ObjectClass.IP_NETWORK, "10.0.0.0/16");
            history.addRevision(objectKey, new Revision(dummyDateTime, dummyDateTime, new DeletedObject(objectKey)));
        }
        {
            ObjectKey objectKey = new ObjectKey(ObjectClass.IP_NETWORK, "10.0.0.0/8");
            history.addRevision(objectKey, new Revision(dummyDateTime, dummyDateTime, new EmptyObject(objectKey)));
        }
        {
            ObjectKey objectKey = new ObjectKey(ObjectClass.IP_NETWORK, "11.0.0.0/8");
            history.addRevision(objectKey, new Revision(dummyDateTime, dummyDateTime, new DeletedObject(objectKey)));
        }

        IpService ipService = new HistoryConfiguration().ipService(history);

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("10.0.0.0/20")).get().getObjectKey().getObjectName(),
                is("10.0.0.0/8")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("11.0.0.0")),
                is(Optional.empty())
        );
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

    static class DeletedObject implements RdapObject {
        private final ObjectKey key;

        public DeletedObject(ObjectKey key) {
            this.key = key;
        }

        @Override
        public ObjectKey getObjectKey() {
            return key;
        }

        @Override
        public boolean isDeleted() {
            return true;
        }
    }
}
