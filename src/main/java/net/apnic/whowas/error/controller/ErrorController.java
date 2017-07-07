package net.apnic.whowas.error.controller;

import net.apnic.whowas.error.MalformedRequestException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global error handling for Spring rest controllers.
 */
@ControllerAdvice
public class ErrorController
{
    /**
     * Catch all least specific exception handlers.
     *
     * Generates an RDAP internal server errror response for the client.
     *
     * @param ex Exception to handle
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handle(Exception ex)
    {
    }

    /**
     * Handler for malformed RDAP request exceptions
     *
     * @param ex MalformedRequestException to handle
     */
    @ExceptionHandler(MalformedRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(MalformedRequestException ex)
    {
    }
}
