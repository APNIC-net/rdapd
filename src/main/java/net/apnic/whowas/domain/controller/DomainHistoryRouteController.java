package net.apnic.whowas.domain.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DomainHistoryRouteController
{
    private final static Logger LOGGER =
        LoggerFactory.getLogger(DomainHistoryRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public DomainHistoryRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
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
            new ObjectKey(ObjectClass.DOMAIN, handle));
    }
}
