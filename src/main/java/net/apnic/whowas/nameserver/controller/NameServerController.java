package net.apnic.whowas.nameserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for the RDAP /nameserver path segment
 *
 * Controller is responsible for dealing with current state RDAP path segments.
 * Currently implemented as a placeholder and returns NOT_IMPLEMENTED http
 * status to querier.
 */
@RestController
@RequestMapping("/nameserver")
public class NameServerController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(NameServerController.class);

    /**
     * GET request handler for nameserver path segment.
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void nameserverPathGet(@PathVariable("handle") String handle)
    {
        LOGGER.info("nameserver GET path query for {}", handle);
    }

    /**
     * HEAD request handler for nameserver path segment.
     */
    @RequestMapping(value="/{handle}", method=RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void nameserverPathHead(@PathVariable("handle") String handle)
    {
        LOGGER.info("nameserver HEAD path query for {}", handle);
    }
}
