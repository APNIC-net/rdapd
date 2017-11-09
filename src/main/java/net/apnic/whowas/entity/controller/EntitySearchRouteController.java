package net.apnic.whowas.entity.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.ObjectSearchType;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.search.SearchResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/entities")
public class EntitySearchRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EntitySearchRouteController.class);

    private final ObjectIndex objectIndex;
    private final ObjectSearchIndex searchIndex;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public EntitySearchRouteController(ObjectIndex objectIndex,
        ObjectSearchIndex searchIndex, RDAPResponseMaker rdapResponseMaker)
    {
        this.objectIndex = objectIndex;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
        this.searchIndex = searchIndex;
    }

    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> entitiesGetPath(
        HttpServletRequest request,
        @RequestParam(name="searchtype", required=false, defaultValue="")
        String searchtype,
        @RequestParam(name="handle", required=false, defaultValue="")
        String handle,
        @RequestParam(name="fn", required=false, defaultValue="")
        String fn)
    {
        LOGGER.info("entities GET path query");

        ObjectSearchType objectSearchType =
              !searchtype.isEmpty() && searchtype.equals("regex")
                ? ObjectSearchType.REGEX
            : searchtype.isEmpty()
                ? ObjectSearchType.STANDARD
                : null;
        if (objectSearchType == null) {
            throw new MalformedRequestException();
        }

        ObjectSearchKey searchKey = null;
        if(handle.isEmpty() == false && fn.isEmpty() == true)
        {
            searchKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
                                            handle.toLowerCase(), objectSearchType);
        }
        else if(handle.isEmpty() == true && fn.isEmpty() == false)
        {
            searchKey = new ObjectSearchKey(ObjectClass.ENTITY, "fn",
                                            fn.toLowerCase(), objectSearchType);
        }
        else
        {
            throw new MalformedRequestException();
        }

        SearchResponse response = null;
        try {
            response = searchIndex.historySearchForObject(searchKey);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw new MalformedRequestException();
            }
            throw e;
        }

        return rdapControllerUtil.searchResponse(request, ObjectClass.ENTITY,
            objectIndex.historyForObject(response.getKeys())
                .filter(oHistory -> oHistory.mostCurrent().isPresent())
                .map(oHistory -> oHistory.mostCurrent().get().getContents()),
                response.isTruncated());
    }
}
