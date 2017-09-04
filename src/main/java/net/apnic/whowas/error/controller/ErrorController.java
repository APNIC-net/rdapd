package net.apnic.whowas.error.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.Error;
import net.apnic.whowas.rdap.TopLevelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global error handling for Spring rest controllers.
 */
@ControllerAdvice
public class ErrorController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    public ErrorController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    /**
     * Catch all least specific exception handlers.
     *
     * Generates an RDAP internal server errror response for the client.
     *
     * @param ex Exception to handle
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TopLevelObject> handle(
        HttpServletRequest request, Exception ex)
    {
        LOGGER.error("Unhandled exception: ", ex);
        return rdapControllerUtil.errorResponseGet(request,
            Error.SERVER_EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handler for malformed RDAP request exceptions
     *
     * @param ex MalformedRequestException to handle
     */
    @ExceptionHandler(MalformedRequestException.class)
    public ResponseEntity<TopLevelObject> handle(HttpServletRequest request,
        MalformedRequestException ex)
    {
        LOGGER.debug("Malformed request received");
        return rdapControllerUtil.errorResponseGet(request,
            Error.MALFORMED_REQUEST, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all Spring exceptions for no handler found.
     *
     * Considered 404 errors
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<TopLevelObject> handle(HttpServletRequest request,
        NoHandlerFoundException ex)
    {
        LOGGER.debug("Not handler found exception");
        return rdapControllerUtil.errorResponseGet(request,
            Error.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
