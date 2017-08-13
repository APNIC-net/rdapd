package net.apnic.whowas.domain.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for the RDAP /domain path segment.
 *
 * This controller is not implemented and returns a HttpStatus code indicating
 * such.
 */
@RestController
@RequestMapping("/domain")
public class DomainRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(DomainRouteController.class);

    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void domainPathGet(@PathVariable("handle") String handle)
    {
        LOGGER.info("domain GET path query for {}", handle);
    }

    @RequestMapping(value="/{handle}", method=RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void domainPathHead()
    {
    }
}
