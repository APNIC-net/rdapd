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
 * Rest controller for the RDAP /history/autnum path segment.
 *
 * Controller is responsible for dealing with history state RDAP path segments.
 */
@RestController
@RequestMapping("/history/autnum")
public class AutnumHistoryRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AutnumHistoryRouteController.class);

    private final AutNumSearchService autnumSearchService;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public AutnumHistoryRouteController(AutNumSearchService autnumSearchService,
        RDAPResponseMaker rdapResponseMaker)
    {
        this.autnumSearchService = autnumSearchService;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    /**
     *
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> autnumPathGet(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.debug("autnum history GET path query for {}", handle);

        try {
            handle = Parsing.parseAutnum(handle);
        } catch(Exception ex) {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.historyResponse(request,
            autnumSearchService.findHistory(ASN.valueOf(Long.parseLong(handle))).orElse(null));
    }
}
