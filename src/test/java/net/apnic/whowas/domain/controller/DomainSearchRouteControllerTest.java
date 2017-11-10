package net.apnic.whowas.domain.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.controller.RDAPControllerTesting;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.search.SearchIndex;
import net.apnic.whowas.search.SearchResponse;

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
@WebMvcTest(DomainSearchRouteController.class)
public class DomainSearchRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.whowas.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @MockBean
    ObjectIndex objectIndex;

    @MockBean
    ObjectSearchIndex objectSearchIndex;

    @Autowired
    private MockMvc mvc;

    @Test
    public void searchHasNameResults()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.of(RDAPControllerTesting.testObjectHistory()));
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/domains?name=*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void malformedForMultiAttributes()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.empty());
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/domains?name=*&nsLdhName=*"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domains?name=*&nsIp=*"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domains?name=*&nsIp=*&nsLdhName=*"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void notImplemented()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.empty());
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/domains?nsLdhName=*"))
            .andExpect(status().isNotImplemented())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domains?nsIp=*"))
            .andExpect(status().isNotImplemented())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(get("/domains?nsIp=*&nsLdhName=*"))
            .andExpect(status().isNotImplemented())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectSearchIndex.historySearchForObject(any())).willThrow(
            new RuntimeException("Test Exception"));

        mvc.perform(get("/domains?name=*"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is("500")));
    }

    @Test
    public void noResultFound()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.empty());
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/domains?name=*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }
}
