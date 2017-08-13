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
 * Rest controller for the RDAP /nameserver path segment
 *
 * Controller is responsible for dealing with current state RDAP path segments.
 */
@RestController
@RequestMapping("/nameserver")
public class NameServerController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(NameServerController.class);

    private RDAPControllerUtil rdapControllerUtil = null;

    @Autowired
    public NameServerController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    /**
     * GET request handler for nameserver path segment.
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> nameserverPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.info("nameserver GET path query for {}", handle);

        return null;
    }
}
