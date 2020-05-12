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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void ipv6PkeyChangingOvertime() {
        // Given
        final History history = new History();
        final String ipPkeyFirstForm = "2001:0DF0:0090::/48";
        final String ipPkeySecondForm = "2001:df0:90::/48";

        final ObjectKey key1 = new ObjectKey(ObjectClass.IP_NETWORK, ipPkeyFirstForm);
        final String country1 = "C1";
        final IpNetwork obj1 = new IpNetwork(key1, Parsing.parseCIDRInterval(ipPkeyFirstForm));
        obj1.setCountry(country1);
        history.addRevision(key1, new Revision(ZonedDateTime.now().minusYears(5), ZonedDateTime.now().minusYears(4),
                obj1));

        final ObjectKey key2 = new ObjectKey(ObjectClass.IP_NETWORK, ipPkeySecondForm);
        final String country2 = "C2";
        final IpNetwork obj2 = new IpNetwork(key2, Parsing.parseCIDRInterval(ipPkeySecondForm));
        obj2.setCountry(country2);
        history.addRevision(key2, new Revision(ZonedDateTime.now().minusYears(3), null,
                obj2));

        IpService ipService = new HistoryConfiguration().ipService(history);

        // When
        Optional<IpNetwork> resultFindFirstForm = ipService.find(Parsing.parseCIDRInterval(ipPkeyFirstForm));
        Optional<IpNetwork> resultFindSecondForm = ipService.find(Parsing.parseCIDRInterval(ipPkeySecondForm));

        // Then
        assertTrue(resultFindFirstForm.isPresent());
        assertThat(resultFindFirstForm.get().getCountry(), is(country2));
        assertTrue(resultFindSecondForm.isPresent());
        assertThat(resultFindSecondForm.get().getCountry(), is(country2));
        assertThat(resultFindFirstForm.get(), is(resultFindSecondForm.get()));
        assertThat(resultFindSecondForm.get().getObjectKey(), is(key1));
        // object key must preserve the first form
        assertThat(resultFindSecondForm.get().getObjectKey().getObjectName(), is(ipPkeyFirstForm));
        // handle must also be preserved
        assertThat(resultFindSecondForm.get().getHandle(), is(ipPkeyFirstForm));
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
