package net.apnic.whowas.entity.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/entities")
public class EntitySearchRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EntitySearchRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public EntitySearchRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    @RequestMapping(method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> entitiesGetPath(
        HttpServletRequest request,
        @RequestParam(name="handle", required=false, defaultValue="")
        String handle,
        @RequestParam(name="fn", required=false, defaultValue="")
        String fn)
    {
        LOGGER.info("entities GET path query");

        ObjectSearchKey searchKey = null;
        if(handle.isEmpty() == false && fn.isEmpty() == true)
        {
            searchKey = new ObjectSearchKey(ObjectClass.ENTITY, "handle",
                                            handle);
        }
        else if(handle.isEmpty() == true && fn.isEmpty() == false)
        {
            searchKey = new ObjectSearchKey(ObjectClass.ENTITY, "fn",
                                            fn);
        }
        else
        {
            throw new MalformedRequestException();
        }

        return rdapControllerUtil.mostCurrentResponseGet(request, searchKey);
    }
}
