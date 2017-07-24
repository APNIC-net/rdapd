package net.apnic.whowas.entity.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
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

@RestController
@RequestMapping("/entity")
public class EntityRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityRouteController.class);

    private RDAPControllerUtil rdapControllerUtil = null;

    @Autowired
    public EntityRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> entityPath(
        HttpServletRequest request,
        @PathVariable("handle") String handle)
    {
        LOGGER.info("entity path query for {}", handle);

        return rdapControllerUtil.mostRecentResponse(
            request, new ObjectKey(ObjectClass.ENTITY, handle));
    }
}
