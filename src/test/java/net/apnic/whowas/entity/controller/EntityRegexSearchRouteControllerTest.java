package net.apnic.whowas.entity.controller;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.controller.RDAPControllerTesting;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rpsl.rdap.RpslToRdap;
import net.apnic.whowas.search.SearchEngine;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EntityRegexSearchRouteControllerTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    History history;

    @Autowired
    SearchEngine searchEngine;

    int allCount = 6;

    @Before
    public void beforeEach() {
        addPerson("Name 1", "N1-AP");
        addPerson("Name 2", "N2-AP");
        addPerson("Name 3", "N3-AP");
        addPerson("John 1", "J1-AP");
        addPerson("John 2", "J2-AP");
        addPerson("John 3", "J3-AP");
    }

    private Revision makePerson(ObjectKey objectKey, String name, String nicHdl)
    {
        RdapObject rdapObject = new RpslToRdap()
            .apply(objectKey, ("person:  " + name + "\n" +
                               "nic-hdl: " + nicHdl + "\n").getBytes());
        Revision revision =
            new Revision(ZonedDateTime.parse("2017-10-18T14:47:31.023+10:00"),
                         null,
                         rdapObject);
        return revision;
    }

    private void addPerson(String name, String nicHdl)
    {
        ObjectKey objectKey = new ObjectKey(ObjectClass.ENTITY, nicHdl);
        Revision revision = makePerson(objectKey, name, nicHdl);
        history.addRevision(objectKey, revision);
        searchEngine.putIndexEntry(revision, objectKey);
    }

    @Test
    public void searchHasHandleResults()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", ".*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(allCount)));
    }

    @Test
    public void searchHasFNResults()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", ".*"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(allCount)));
    }

    @Test
    public void searchPlus()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", ".+"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(allCount)));
    }

    @Test
    public void searchOptional()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", ".?.?.?.?.?.?"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(allCount)));
    }

    @Test
    public void searchCharacters()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", "N[1-2]-AP"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(2)));
    }

    @Test
    public void searchCounts()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", "[A-Z0-9]{2}-AP"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(allCount)));
    }

    @Test
    public void searchNegatedCharacters()
        throws Exception
    {
        mvc.perform(
            get("/entities?handle={handle}&searchtype=regex", "N[^A-Z01]-AP"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(2)));
    }

    @Test
    public void searchAlternatives()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "(Name|John) 1"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(2)));
    }

    @Test
    @Ignore
    public void searchCharacterClasses()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "Name [[:digit:]]"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(3)));
    }

    @Test
    public void searchCaseInsensitive()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "(name|john) 1"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(2)));
    }

    @Test
    public void searchSubpatternMatches()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "john"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(3)));
    }

    @Test
    public void searchAnchorInitial()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "^John"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(3)));
    }

    @Test
    public void searchAnchorFinal()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "3$"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(2)));
    }

    @Test
    public void searchAnchorBoth()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "^John 1$"))
            .andExpect(status().isOk())
            .andExpect(RDAPControllerTesting.isRDAP())
            .andExpect(jsonPath("$.entitySearchResults", hasSize(1)));
    }

    @Test
    public void searchInvalidRegex()
        throws Exception
    {
        mvc.perform(
            get("/entities?fn={fn}&searchtype=regex", "[asdf"))
            .andExpect(status().isBadRequest())
            .andExpect(RDAPControllerTesting.isRDAP());
    }
}
