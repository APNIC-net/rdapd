package net.apnic.whowas.entity.controller;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;

import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.controller.RDAPControllerTesting;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;

import static org.hamcrest.Matchers.is;

import org.junit.Assert;
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

import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EntityRouteController.class)
public class EntityRouteControllerTest
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

        mvc.perform(get("/entity/ABC123"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/entity/ABC123"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void runtimeExceptionIs500()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willThrow(new RuntimeException("Test Exception"));

        mvc.perform(get("/entity/ABC123"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.errorCode", is("500")));

        mvc.perform(head("/entity/ABC123"))
            .andExpect(status().isInternalServerError())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void noResultFound()
        throws Exception
    {
        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willReturn(Optional.empty());

        mvc.perform(get("/entity/ABC123"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAP());

        mvc.perform(head("/entity/ABC123"))
            .andExpect(status().isNotFound())
            .andExpect(RDAPControllerTesting.isRDAPHeader());
    }

    @Test
    public void noAuthAttributeFound()
        throws Exception
    {
        ObjectKey objectKey = new ObjectKey(ObjectClass.ENTITY, "super-mnt");
        RdapObject rdapObject = new RpslToRdap()
            .apply(objectKey, "mntner:  Super Mnt\nhandle: super-mnt\nauth: CRYPT-PW  secretpass\n".getBytes());
        Revision revision = new Revision(
                ZonedDateTime.parse("2017-10-18T14:47:31.023+10:00"),
                null,
                rdapObject);
        ObjectHistory objectHistory = new ObjectHistory(objectKey).appendRevision(revision);

        given(objectIndex.historyForObject(any(ObjectKey.class)))
            .willReturn(Optional.of(objectHistory));

        MvcResult result = mvc.perform(get("/entity/ABC123"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andReturn();

        String content = result.getResponse().getContentAsString();
        Assert.assertFalse(content.contains("secretpass"));
    }
}
