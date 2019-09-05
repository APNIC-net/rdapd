package net.apnic.rdapd.loaders.rpsldata;

import net.apnic.rdapd.history.History;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.history.ObjectKey;
import org.junit.Test;

import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class RpslLoaderTest {

    private final static String AUTNUM1 = "AS10034";
    private final static String AUTNUM2 = "AS10035";
    private final static String INETNUM1 = "1.11.0.0 - 1.11.255.255";
    private final static String INETNUM2 = "1.16.0.0 - 1.16.63.255";
    private final static String INET6NUM1 = "2001:0220::/32";
    private final static String INET6NUM2 = "2001:0230::/32";
    private final static String ENTITY1 = "AM5691-KR";
    private final static String ENTITY2 = "AM5693-KR";
    private final static String ENTITY3 = "IRT-KRNIC-KR";

    @Test
    public void testAutnumLoad() {
        // Given
        final History history = new History();
        final RpslLoader rpslLoader = createRpslLoader(history, "rpsl/autnum.db");

        // When
        rpslLoader.initialise();

        // Then
        Optional<ObjectHistory> autnum1Revisions = history.historyForObject(new ObjectKey(ObjectClass.AUT_NUM, AUTNUM1));
        assertThat(autnum1Revisions.isPresent(), is(true));
        assertThat(autnum1Revisions.get().mostCurrent().isPresent(), is(true));

        Optional<ObjectHistory> autnum2Revisions = history.historyForObject(new ObjectKey(ObjectClass.AUT_NUM, AUTNUM2));
        assertThat(autnum2Revisions.isPresent(), is(true));
        assertThat(autnum2Revisions.get().mostCurrent().isPresent(), is(true));
    }

    @Test
    public void testInetnumLoad() {
        // Given
        final History history = new History();
        final RpslLoader rpslLoader = createRpslLoader(history, "rpsl/inetnum.db");

        // When
        rpslLoader.initialise();

        // Then
        Optional<ObjectHistory> inetnum1Revisions = history.historyForObject(new ObjectKey(ObjectClass.IP_NETWORK,
                INETNUM1));
        assertThat(inetnum1Revisions.isPresent(), is(true));
        assertThat(inetnum1Revisions.get().mostCurrent().isPresent(), is(true));

        Optional<ObjectHistory> inetnum2Revisions = history.historyForObject(new ObjectKey(ObjectClass.IP_NETWORK,
                INETNUM2));
        assertThat(inetnum2Revisions.isPresent(), is(true));
        assertThat(inetnum2Revisions.get().mostCurrent().isPresent(), is(true));
    }

    @Test
    public void testIne6tnumLoad() {
        // Given
        final History history = new History();
        final RpslLoader rpslLoader = createRpslLoader(history, "rpsl/inet6num.db");

        // When
        rpslLoader.initialise();

        // Then
        Optional<ObjectHistory> inet6num1Revisions = history.historyForObject(new ObjectKey(ObjectClass.IP_NETWORK,
                INET6NUM1));
        assertThat(inet6num1Revisions.isPresent(), is(true));
        assertThat(inet6num1Revisions.get().mostCurrent().isPresent(), is(true));

        Optional<ObjectHistory> inet6num2Revisions = history.historyForObject(new ObjectKey(ObjectClass.IP_NETWORK,
                INET6NUM2));
        assertThat(inet6num2Revisions.isPresent(), is(true));
        assertThat(inet6num2Revisions.get().mostCurrent().isPresent(), is(true));
    }

    @Test
    public void testEntityLoad() {
        // Given
        final History history = new History();
        final RpslLoader rpslLoader = createRpslLoader(history, "rpsl/entity.db");

        // When
        rpslLoader.initialise();

        // Then
        Optional<ObjectHistory> entity1Revisions = history.historyForObject(new ObjectKey(ObjectClass.ENTITY,
                ENTITY1));
        assertThat(entity1Revisions.isPresent(), is(true));
        assertThat(entity1Revisions.get().mostCurrent().isPresent(), is(true));

        Optional<ObjectHistory> entity2Revisions = history.historyForObject(new ObjectKey(ObjectClass.ENTITY,
                ENTITY2));
        assertThat(entity2Revisions.isPresent(), is(true));
        assertThat(entity2Revisions.get().mostCurrent().isPresent(), is(true));

        Optional<ObjectHistory> entity3Revisions = history.historyForObject(new ObjectKey(ObjectClass.ENTITY,
                                                                                          ENTITY3));
        assertThat(entity3Revisions.isPresent(), is(true));
        assertThat(entity3Revisions.get().mostCurrent().isPresent(), is(true));
    }

    private RpslLoader createRpslLoader(History history, String rpslFile) {
        URL resource = getClass().getClassLoader().getResource(rpslFile);
        RpslConfig config = new RpslConfig();
        config.setUri(resource.toExternalForm());
        return new RpslLoader(history, config);
    }

}