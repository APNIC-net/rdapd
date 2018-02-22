package net.apnic.rdapd.autnum.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.autnum.ASN;
import net.apnic.whowas.autnum.AutNumSearchService;
import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.types.Parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for the RDAP /autnum path segment.
 *
 * Controller is reponsible for dealing with current state RDAP path segments.
 */
@RestController
@RequestMapping("/autnum")
public class AutnumRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AutnumRouteController.class);

    private AutNumSearchService autnumSearchService;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public AutnumRouteController(AutNumSearchService autnumSearchService,
        RDAPResponseMaker rdapResponseMaker)
    {
        this.autnumSearchService = autnumSearchService;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    /**
     * GET request handler for autnum path segment.
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> autnumPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("autnum GET path query for {}", handle);

        try {
            handle = Parsing.parseAutnum(handle);
        } catch(Exception ex) {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.singleObjectResponse(request,
            autnumSearchService.findCurrent(ASN.valueOf(Long.parseLong(handle))).orElse(null));
    }

    /**
     * HEAD request handler for autnum path segment.
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.HEAD)
    public ResponseEntity<TopLevelObject> autnumPathHead(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("autnum HEAD path query for {}", handle);

        try {
            handle = Parsing.parseAutnum(handle);
        } catch(Exception ex) {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.singleObjectResponse(request,
            autnumSearchService.findCurrent(ASN.valueOf(Long.parseLong(handle))).orElse(null));
    }
}
