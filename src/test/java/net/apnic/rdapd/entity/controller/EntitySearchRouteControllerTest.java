package net.apnic.rdapd.entity.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.history.ObjectIndex;
import net.apnic.rdapd.history.ObjectSearchIndex;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.rdap.controller.RDAPControllerTesting;
import net.apnic.rdapd.rdap.controller.RDAPResponseMaker;
import net.apnic.rdapd.search.SearchIndex;
import net.apnic.rdapd.search.SearchResponse;

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
@WebMvcTest(EntitySearchRouteController.class)
public class EntitySearchRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.rdapd.rdap.config")
    static class TestRDAPControllerConfiguration {}

    @MockBean
    ObjectIndex objectIndex;

    @MockBean
    ObjectSearchIndex objectSearchIndex;

    @Autowired
    private MockMvc mvc;

    @Test
    public void searchHasHandleResults()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.of(RDAPControllerTesting.testObjectHistory()));
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/entities?handle=*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void searchHasFNResults()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.of(RDAPControllerTesting.testObjectHistory()));
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/entities?fn=*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void malformedForMultiAttributes()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.of(RDAPControllerTesting.testObjectHistory()));
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/entities?handle=*&fn=*"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void malformedForJunkAttributes()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.of(RDAPControllerTesting.testObjectHistory()));
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/entities?junk=junk"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectSearchIndex.historySearchForObject(any()))
            .willThrow(new RuntimeException("Test Exception"));

        mvc.perform(get("/entities?handle=*"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(500)));
    }

    @Test
    public void noResultFound()
        throws Exception
    {
        given(objectIndex.historyForObject(any(Stream.class))).willReturn(
            Stream.empty());
        given(objectSearchIndex.historySearchForObject(any())).willReturn(
            SearchResponse.makeEmpty());

        mvc.perform(get("/entities?handle=*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());
    }
}
