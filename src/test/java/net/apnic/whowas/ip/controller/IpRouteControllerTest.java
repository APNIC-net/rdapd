package net.apnic.whowas.ip.controller;

import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.intervaltree.IntervalTree;
import static net.apnic.whowas.rdap.controller.RDAPControllerTesting.testObjectHistory;
import static net.apnic.whowas.rdap.controller.RDAPControllerTesting.isRDAP;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
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
    IntervalTree<IP, ObjectHistory, IpInterval> historyTree;

    @Autowired
    private MockMvc mvc;

    @Test
    public void indexLookupHasResults()
        throws Exception
    {
        given(historyTree.equalToAndLeastSpecific(any())).willReturn(
            Stream.of(new Tuple<>(null, testObjectHistory())));

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isOk())
            .andExpect(isRDAP());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(historyTree.equalToAndLeastSpecific(any())).willThrow(
            new RuntimeException("Test exception"));

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isInternalServerError())
            .andExpect(isRDAP())
            .andExpect(jsonPath("$.errorCode", is("500")));
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse()
        throws Exception
    {
        given(historyTree.equalToAndLeastSpecific(any())).willReturn(Stream.empty());

        mvc.perform(get("/ip/10.0.0.0"))
            .andExpect(status().isNotFound())
            .andExpect(isRDAP());
    }
}
