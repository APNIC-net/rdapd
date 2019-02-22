package net.apnic.rdapd.ip.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.ip.IpService;
import net.apnic.rdapd.rdap.IpNetwork;
import net.apnic.rdapd.types.Parsing;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static net.apnic.rdapd.rdap.controller.RDAPControllerTesting.isRDAP;
import static net.apnic.rdapd.rdap.controller.RDAPControllerTesting.isRDAPHeader;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(IpRouteController.class)
public class IpRouteControllerTest
{
    @TestConfiguration
    @ComponentScan(basePackages="net.apnic.rdapd.rdap.config")
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

    @Test
    public void testCidr0Ipv4() throws Exception {
        given(ipService.find(any())).willReturn(
                Optional.of(new IpNetwork(
                        new ObjectKey(ObjectClass.IP_NETWORK, "10.0.0.0 - 10.255.255.255"),
                        Parsing.parseInterval("10.0.0.0/8"))));

        MvcResult mvcResult = mvc.perform(get("/ip/10.0.0.0"))
                .andExpect(status().isOk())
                .andExpect(isRDAP())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(content);

        assertThat(json.get("cidr0_cidrs"), notNullValue());
        assertThat(json.get("cidr0_cidrs").size(), is(1));
        assertThat(json.get("cidr0_cidrs").get(0).get("v4prefix").textValue(), is("10.0.0.0"));
        assertThat(json.get("cidr0_cidrs").get(0).get("length").intValue(), is(8));
    }

    @Test
    public void testCidr0Ipv6() throws Exception {
        given(ipService.find(any())).willReturn(
                Optional.of(new IpNetwork(
                        new ObjectKey(ObjectClass.IP_NETWORK, "2001:db8:: - 2001:db8:0:ffff:ffff:ffff:ffff:ffff"),
                        Parsing.parseInterval("2001:db8::/32"))));

        MvcResult mvcResult = mvc.perform(get("/ip/2001:db8::"))
                .andExpect(status().isOk())
                .andExpect(isRDAP())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(content);

        assertThat(json.get("cidr0_cidrs"), notNullValue());
        assertThat(json.get("cidr0_cidrs").size(), is(1));
        assertThat(json.get("cidr0_cidrs").get(0).get("v6prefix").textValue(), is("2001:db8::"));
        assertThat(json.get("cidr0_cidrs").get(0).get("length").intValue(), is(32));
    }
}
