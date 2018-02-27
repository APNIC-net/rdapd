package net.apnic.rdapd.rdap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.rdap.AutNum;
import net.apnic.rdapd.rdap.RdapObject;
import net.apnic.rdapd.rpsl.rdap.RpslToRdap;

import org.springframework.mock.web.MockHttpServletResponse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

public class RDAPControllerTesting
{
    public static AutNum testAutNumObject() {
        ObjectKey objectKey = new ObjectKey(ObjectClass.AUT_NUM, "AS1234");
        AutNum rval = new AutNum(objectKey);
        rval.setASNInterval("1234", "1234");
        return rval;
    }

    public static ObjectHistory testObjectHistory() {
        ObjectKey objectKey = new ObjectKey(ObjectClass.ENTITY, "example");
        RdapObject rdapObject = new RpslToRdap()
                .apply(objectKey, "person:  Example Citizen\nhandle:EC44-AP\n".getBytes());
        Revision revision = new Revision(
                ZonedDateTime.parse("2017-10-18T14:47:31.023+10:00"),
                null,
                rdapObject);
        ObjectHistory objectHistory = new ObjectHistory(objectKey).appendRevision(revision);
        return objectHistory;
    }

    public static ResultMatcher isRDAP() {
        return compositeResultMatcher(
                header().string("Content-Type", "application/rdap+json"),
                jsonPath("$.rdapConformance", not(empty()))
        );
    }

    public static ResultMatcher isRDAPHeader() {
        return compositeResultMatcher(
                header().string("Content-Type", "application/rdap+json"));
    }

    public static ResultMatcher compositeResultMatcher(final ResultMatcher... matchers) {
        return mvcResult -> {
            for(ResultMatcher resultMatcher : matchers) {
                resultMatcher.match(mvcResult);
            }
        };
    }

    public void printResponse(MockHttpServletResponse response) {
        response.getHeaderNames().forEach(headerName ->
                System.out.println(headerName + ": " + response.getHeaders(headerName)));
        try {
            System.out.println(prettyJson(response.getContentAsString()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object jsonObject = mapper.readValue(json, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            return prettyJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

