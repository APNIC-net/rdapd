package net.apnic.whowas.nameserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for the RDAP /nameservers search path segment.
 *
 * Controller is responsible for dealing with current state RDAP path segments.
 * Currently implemented as a placeholder and returns HTTP Not Implemented.
 */
@RestController
@RequestMapping("/nameservers")
public class NameServersController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(NameServersController.class);

    /**
     * GET request handler for nameserver search path segment.
     */
    @RequestMapping(method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void nameserverSearchPathGet()
    {
        LOGGER.info("nameservers GET path query");
    }
}
