package net.apnic.whowas.autnum.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.autnum.util.AutnumUtil;
import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.rdap.TopLevelObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autnum")
public class AutnumRouteController
{
    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> autnumPath(HttpServletRequest request,
                                                     @PathVariable("handle")
                                                     String handle)
    {
        if(AutnumUtil.isValidAutnum(handle) == false)
        {
            throw new MalformedRequestException();
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
