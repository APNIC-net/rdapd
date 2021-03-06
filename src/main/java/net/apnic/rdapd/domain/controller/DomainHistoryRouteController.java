package net.apnic.rdapd.domain.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.rdapd.rdap.controller.RDAPControllerUtil;
import net.apnic.rdapd.rdap.controller.RDAPResponseMaker;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectIndex;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for the RDAP /history/domain path segment.
 *
 * Controller is responsible for dealing with history state RDAP path segments.
 */
@RestController
@RequestMapping("/history/domain")
@ConditionalOnProperty(value = "rdap.historyEndpointsEnabled", matchIfMissing = true)
public class DomainHistoryRouteController
{
    private final static Logger LOGGER =
        LoggerFactory.getLogger(DomainHistoryRouteController.class);

    private final ObjectIndex objectIndex;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public DomainHistoryRouteController(ObjectIndex objectIndex,
        RDAPResponseMaker rdapResponseMaker)
    {
        this.objectIndex = objectIndex;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    /**
     *
     */
    @RequestMapping(value="/{handle:.+}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> domainPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("domain history GET path query for {}", handle);

        return rdapControllerUtil.historyResponse(request,
            objectIndex.historyForObject(
                new ObjectKey(ObjectClass.DOMAIN, handle)).orElse(null));
    }
}
