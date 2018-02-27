package net.apnic.rdapd.autnum.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.rdapd.autnum.ASN;
import net.apnic.rdapd.autnum.AutNumSearchService;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.rdap.controller.RDAPControllerTesting;
import net.apnic.rdapd.rdap.controller.RDAPResponseMaker;

import static org.hamcrest.Matchers.is;

import org.junit.runner.RunWith;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AutnumHistoryRouteController.class)
public class AutnumHistoryRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.rdapd.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @MockBean
    AutNumSearchService autnumSearchService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void indexLookupHasResults()
        throws Exception
    {
        given(autnumSearchService.findHistory(any(ASN.class))).willReturn(
            Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/history/autnum/1234"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/history/autnum/123456789"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    /**
     * Regression test to make sure that 'AS1234' is no longer supported.
     *
     * Background previous versions only worked with autnums that where prefixed
     * with 'AS'
     */
    @Test
    public void indexLookupDoesNotSupportASPrefix()
        throws Exception
    {
        given(autnumSearchService.findHistory(any(ASN.class))).willReturn(
            Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/history/autnum/AS1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void malformedRequest()
        throws Exception
    {
        given(autnumSearchService.findHistory(any(ASN.class))).willReturn(
            Optional.empty());

        mvc.perform(get("/history/autnum/notanint"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/history/autnum/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/history/autnum/0"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        // 1 more than what a 4 byte unsigned int can support
        mvc.perform(get("/history/autnum/4294967296"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse()
        throws Exception
    {
        given(autnumSearchService.findHistory(any(ASN.class))).willReturn(
            Optional.empty());

        mvc.perform(get("/history/autnum/1234"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(autnumSearchService.findHistory(any(ASN.class))).willThrow(
            new RuntimeException("Test exception"));

        mvc.perform(get("/history/autnum/1234"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP());
    }
}
