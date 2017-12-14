package net.apnic.whowas.help.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/help")
public class HelpRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(HelpRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public HelpRouteController(RDAPResponseMaker rdapResponseMaker)
    {
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> helpPathGet(HttpServletRequest request)
    {
        LOGGER.debug("help GET path query");
        return rdapControllerUtil.makeGenericResponse(request, null);
    }

    @RequestMapping(method=RequestMethod.HEAD)
    public ResponseEntity<TopLevelObject> helpPathHead(HttpServletRequest request)
    {
        LOGGER.debug("help HEAD path query");
        return rdapControllerUtil.makeGenericResponse(request, null);
    }
}
