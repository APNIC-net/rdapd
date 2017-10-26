package net.apnic.whowas.domain.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for the RDAP /domains search path segment.
 *
 * This controller is not implemented and returns a HttpStatus code indicating
 * such.
 */
@RestController
@RequestMapping("/domains")
public class DomainSearchRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(DomainSearchRouteController.class);

    private final ObjectIndex objectIndex;
    private final RDAPControllerUtil rdapControllerUtil;
    private final ObjectSearchIndex searchIndex;

    @Autowired
    public DomainSearchRouteController(ObjectIndex objectIndex,
        ObjectSearchIndex searchIndex, RDAPResponseMaker rdapResponseMaker)
    {
        this.objectIndex = objectIndex;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
        this.searchIndex = searchIndex;
    }

    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> domainsPathGet(
        HttpServletRequest request,
        @RequestParam(name="name", required=true, defaultValue="")
        String name,
        @RequestParam(name="nsLdhName", required=false, defaultValue="")
        String nsLdhName,
        @RequestParam(name="nsIp", required=false, defaultValue="")
        String nsIp)
    {
        LOGGER.debug("domains GET path query");

        // If name is specificed and no other parameters
        if(name.isEmpty() == false && nsLdhName.isEmpty() && nsIp.isEmpty())
        {
            ObjectSearchKey searchKey = new ObjectSearchKey(ObjectClass.DOMAIN,
                "name", name);

            return rdapControllerUtil.searchResponse(request, ObjectClass.DOMAIN,
                objectIndex.historyForObject(
                    searchIndex.historySearchForObject(searchKey))
                    .filter(oHistory -> oHistory.mostCurrent().isPresent())
                    .map(oHistory -> oHistory.mostCurrent().get().getContents()));
        }
        // If name is not specificed and no other parameters exist
        else if(name.isEmpty() && (nsLdhName.isEmpty() == false ||
                nsIp.isEmpty() == false))
        {
            return rdapControllerUtil.notImplementedResponseGet(request);
        }

        // If nothing from above then we have a malformed request
        throw new MalformedRequestException();
    }
}
