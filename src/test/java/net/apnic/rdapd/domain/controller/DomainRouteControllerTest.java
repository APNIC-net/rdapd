package net.apnic.rdapd.domain.controller;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectIndex;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.rdap.controller.RDAPControllerTesting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DomainRouteController.class)
public class DomainRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.rdapd.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @MockBean
    ObjectIndex objectIndex;

    @Autowired
    private MockMvc mvc;

    @Test
    public void indexLookupHasResults()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(
            Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(head("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willThrow(new RuntimeException("Test Exception"));

        mvc.perform(get("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(500)));

        mvc.perform(get("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(500)));

        mvc.perform(head("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(head("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void noResultFound()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willReturn(Optional.empty());

        mvc.perform(get("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(404)));

        mvc.perform(get("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(404)));

        mvc.perform(head("/domain/1.2.3.10.in-addr.arpa"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(head("/domain/0.2.e.0.1.0.0.2.ip6.arpa"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void testMostSpecificIPv4RDNSQueries() throws Exception {
        final String IPV4_RDNS_DOMAIN_ROOT = "in-addr.arpa";
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(Optional.empty());
        given(objectIndex.historyForObject(new ObjectKey(ObjectClass.DOMAIN, "100." + IPV4_RDNS_DOMAIN_ROOT)))
                .willReturn(Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/domain/100.in-addr.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/50.100.in-addr.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/25.50.100.in-addr.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/1.25.50.100.in-addr.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/101.in-addr.arpa"))
                .andExpect(status().isNotFound())
                .andExpect(RDAPControllerTesting.isRDAP())
                .andExpect(jsonPath("$.errorCode", is(404)));
    }

    @Test
    public void testMostSpecificIPv6RDNSQueries() throws Exception {
        final String IPV6_RDNS_DOMAIN_ROOT = "ip6.arpa";
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(Optional.empty());
        given(objectIndex.historyForObject(new ObjectKey(ObjectClass.DOMAIN, "8.1.0.0.2." + IPV6_RDNS_DOMAIN_ROOT)))
                .willReturn(Optional.of(RDAPControllerTesting.testObjectHistory()));

       mvc.perform(get("/domain/8.1.0.0.2.ip6.arpa"))
               .andExpect(status().isOk())
               .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/0.8.1.0.0.2.ip6.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/3.0.8.1.0.0.2.ip6.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/a.3.0.8.1.0.0.2.ip6.arpa"))
                .andExpect(status().isOk())
                .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domain/1.0.0.2.ip6.arpa"))
                .andExpect(status().isNotFound())
                .andExpect(RDAPControllerTesting.isRDAP())
                .andExpect(jsonPath("$.errorCode", is(404)));
    }
}
