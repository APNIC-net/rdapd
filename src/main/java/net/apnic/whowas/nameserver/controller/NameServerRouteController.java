package net.apnic.whowas.nameserver.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for the RDAP /nameserver path segment
 *
 * Controller is responsible for dealing with current state RDAP path segments.
 * Currently implemented as a placeholder and returns HTTP Not Implemented.
 */
@RestController
@RequestMapping("/nameserver")
public class NameServerRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(NameServerRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public NameServerRouteController(RDAPResponseMaker rdapResponseMaker)
    {
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    /**
     * GET request handler for nameserver path segment.
     */
    @RequestMapping(value="/{handle:.+}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> nameserverPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("nameserver GET path query for {}", handle);
        return rdapControllerUtil.notImplementedResponseGet(request);
    }

    /**
     * HEAD request handler for nameserver path segment.
     */
    @RequestMapping(value="/{handle:.+}", method=RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<TopLevelObject> nameserverPathHead(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("nameserver HEAD path query for {}", handle);
        return rdapControllerUtil.notImplementedResponseGet(request);
    }
}
