package net.apnic.whowas.nameserver.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
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
 * Rest controller for the RDAP /history/nameserver path segment.
 *
 * Controller is responsible for dealing with history state RDAP path segments.
 * Currently implemented as a placeholder and returns HTTP Not Implemented.
 */
@RestController
@RequestMapping("/history/nameserver")
public class NameServerHistoryRouteController
{
    private final static Logger LOGGER =
        LoggerFactory.getLogger(NameServerHistoryRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public NameServerHistoryRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    /**
     * GET request handler for nameserver history path segment.
     */
    @RequestMapping(value="/{handle:.+}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> nameserverPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("nameserver history GET path query for {}", handle);
        return rdapControllerUtil.notImplementedResponseGet(request);
    }
}
