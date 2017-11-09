package net.apnic.whowas.autnum.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.controller.RDAPControllerTesting;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;

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
@WebMvcTest(AutnumRouteController.class)
public class AutnumRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.whowas.rdap.config")
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

        mvc.perform(get("/autnum/1234"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/autnum/123456789"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/1234"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(head("/autnum/123456789"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
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
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(
            Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/autnum/AS1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/AS1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void indexLookupDoesNotSupportASRanges()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(
            Optional.of(RDAPControllerTesting.testObjectHistory()));

        mvc.perform(get("/autnum/123-1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/autnum/AS123-AS1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/autnum/AS123-1234"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void malformedRequest()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(
            Optional.empty());

        mvc.perform(get("/autnum/notanint"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/notanint"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(get("/autnum/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        mvc.perform(get("/autnum/0"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/0"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAPHeader());

        // 1 more than what a 4 byte unsigned int can support
        mvc.perform(get("/autnum/4294967296"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/4294967296"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void noSearchResultsIsNotFoundRDAPResponse()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class))).willReturn(
            Optional.empty());

        mvc.perform(get("/autnum/1234"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/1234"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class))).willThrow(
            new RuntimeException("Test exception"));

        mvc.perform(get("/autnum/1234"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/autnum/1234"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }
}
