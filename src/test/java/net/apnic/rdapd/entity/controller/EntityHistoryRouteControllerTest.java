package net.apnic.rdapd.entity.controller;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.history.ObjectIndex;
import net.apnic.rdapd.history.ObjectKey;
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
@WebMvcTest(EntityHistoryRouteController.class)
public class EntityHistoryRouteControllerTest
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

        mvc.perform(get("/history/entity/ABC123"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(RDAPControllerTesting.isRDAPHistoryResult());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willThrow(new RuntimeException("Test Exception"));

        mvc.perform(get("/history/entity/ABC123"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is(500)));
    }

    @Test
    public void noResultFound()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willReturn(Optional.empty());

        mvc.perform(get("/history/entity/ABC123"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP());
    }
}
