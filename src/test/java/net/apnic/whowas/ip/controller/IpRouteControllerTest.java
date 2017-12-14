package net.apnic.whowas.ip.controller;

import java.util.Optional;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.ip.IpService;
import static net.apnic.whowas.rdap.controller.RDAPControllerTesting.testObjectHistory;
import static net.apnic.whowas.rdap.controller.RDAPControllerTesting.isRDAP;
import static net.apnic.whowas.rdap.controller.RDAPControllerTesting.isRDAPHeader;
import net.apnic.whowas.rdap.IpNetwork;
import net.apnic.whowas.types.Parsing;
import net.apnic.whowas.types.Tuple;

import static org.hamcrest.Matchers.is;

import org.junit.runner.RunWith;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(IpRouteController.class)
public class IpRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @MockBean
    IpService ipService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void indexLookupHasResults()
        throws Exception
    {
        given(ipService.find(any())).willReturn(
            Optional.of(new IpNetwork(
                new ObjectKey(ObjectClass.IP_NETWORK, "10.0.0.0 - 10.255.255.255"),
                Parsing.parseInterval("10.0.0.0/8"))));

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isOk())
            .andExpect(isRDAP());

        mvc.perform(head("/ip/10.0.0.0"))
            .andExpect(status().isOk())
            .andExpect(isRDAPHeader());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(ipService.find(any())).willThrow(
            new RuntimeException("Test exception"));

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isInternalServerError())
            .andExpect(isRDAP())
            .andExpect(jsonPath("$.errorCode", is(500)));
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse()
        throws Exception
    {
        given(ipService.find(any())).willReturn(Optional.empty());

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isNotFound())
            .andExpect(isRDAP());
    }

    @Test
    public void malformedRequest()
        throws Exception
    {
        given(ipService.find(any())).willReturn(Optional.empty());

        mvc.perform(get("/ip/bad-ip"))
            .andExpect(status().isBadRequest())
            .andExpect(isRDAP());

        mvc.perform(get("/ip/10.1.1.2/16"))
            .andExpect(status().isBadRequest())
            .andExpect(isRDAP());

        mvc.perform(get("/ip/2001:abcd::/16"))
            .andExpect(status().isBadRequest())
            .andExpect(isRDAP());
    }
}
