package net.apnic.whowas.domain.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
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
 * Rest Controller for the RDAP /domain path segment.
 *
 * Controller is responsible for dealing with current state RDAP path segments.
 */
@RestController
@RequestMapping("/domain")
public class DomainRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(DomainRouteController.class);

    private RDAPControllerUtil rdapControllerUtil = null;

    @Autowired
    public DomainRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    @RequestMapping(value="/{handle:.+}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> domainPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.info("domain GET path query for {}", handle);

        return rdapControllerUtil.mostCurrentResponseGet(
            request, new ObjectKey(ObjectClass.DOMAIN, handle));
    }

    @RequestMapping(value="/{handle:.+}", method=RequestMethod.HEAD)
    public ResponseEntity<Void> domainPathHead(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.info("domain HEAD path query for {}", handle);

        return rdapControllerUtil.mostCurrentResponseHead(
            request, new ObjectKey(ObjectClass.DOMAIN, handle));
    }
}
