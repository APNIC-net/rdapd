package net.apnic.rdapd.ip;

import net.apnic.rdapd.history.History;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.history.config.HistoryConfiguration;
import net.apnic.rdapd.rdap.IpNetwork;
import net.apnic.rdapd.types.Parsing;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IpServiceTest {

    ZonedDateTime dummyDateTime = null;

    @Test
    public void findMostSpecificEncompassingRange() {
        History history = new History();
        for(String s : Arrays.asList(
                "10.0.0.0/8",
                "10.0.0.0/16",
                "10.0.0.0/22",
                "10.0.0.0/24",
                "10.0.1.0/24",
                "101.0.0.0/8",
                "101.0.0.0/22",
                "59.0.0.0/8",
                "59.167.0.0/16",
                "2001:4400::/23",
                "2001:4400:abcd::/48",
                "2001:8000::/19"
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
                ipService.find(Parsing.parseCIDRInterval("101.0.0.6")).get().getObjectKey().getObjectName(),
                is("101.0.0.0/22")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("101.0.0.0/24")).get().getObjectKey().getObjectName(),
                is("101.0.0.0/22")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("101.0.0.0/22")).get().getObjectKey().getObjectName(),
                is("101.0.0.0/22")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("59.167.223.68")).get().getObjectKey().getObjectName(),
                is("59.167.0.0/16")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("59.167.228.0/24")).get().getObjectKey().getObjectName(),
                is("59.167.0.0/16")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("59.167.0.0/24")).get().getObjectKey().getObjectName(),
                is("59.167.0.0/16")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("59.167.0.0/16")).get().getObjectKey().getObjectName(),
                is("59.167.0.0/16")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("0.0.0.0/0")),
                is(Optional.empty())
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("2001:4400::/23")).get().getObjectKey().getObjectName(),
                is("2001:4400::/23")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("2001:4400:1::/48")).get().getObjectKey().getObjectName(),
                is("2001:4400::/23")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("2001:4400:abcd::/48")).get().getObjectKey().getObjectName(),
                is("2001:4400:abcd::/48")
        );

        assertThat(
                ipService.find(Parsing.parseCIDRInterval("2001:8000::/19")).get().getObjectKey().getObjectName(),
                is("2001:8000::/19")
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

    static class EmptyObject extends IpNetwork {
        public EmptyObject(ObjectKey key) {
            super(key, Parsing.parseCIDRInterval(key.getObjectName()));
        }
    }

    static class DeletedObject extends IpNetwork {
        public DeletedObject(ObjectKey key) {
            super(key, Parsing.parseCIDRInterval(key.getObjectName()));
        }

        @Override
        public boolean isDeleted() {
            return true;
        }
    }
}
