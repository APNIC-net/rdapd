package net.apnic.whowas.domain.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for the RDAP /domains search path segment.
 *
 * This controller is not implemented and returns a HttpStatus code indicating
 * such.
 */
@RestController
@RequestMapping("/domains")
public class DomainsRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(DomainsRouteController.class);

    @RequestMapping(method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void domainsPathGet()
    {
        LOGGER.info("domains GET path query");
    }
}
