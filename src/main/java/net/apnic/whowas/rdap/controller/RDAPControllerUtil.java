package net.apnic.whowas.rdap.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.rdap.Error;
import net.apnic.whowas.rdap.http.RdapConstants;
import net.apnic.whowas.rdap.RdapHistory;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rdap.RdapSearch;
import net.apnic.whowas.rdap.TopLevelObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * A set of helper functions and utilities to make RDAP results for controllers.
 */
public class RDAPControllerUtil
{
    private HttpHeaders responseHeaders = null;
    private final RDAPResponseMaker responseMaker;

    /**
     * Default constructor.
     */
    public RDAPControllerUtil(RDAPResponseMaker responseMaker)
    {
        setupResponseHeaders();
        this.responseMaker = responseMaker;
    }

    public ResponseEntity<TopLevelObject> errorResponseGet(
        HttpServletRequest request, Error error, HttpStatus status)
    {
        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(error, request),
            responseHeaders,
            status);
    }

    public ResponseEntity<TopLevelObject> notImplementedResponseGet(
        HttpServletRequest request)
    {
        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(Error.NOT_IMPLEMENTED, request),
            responseHeaders,
            HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<TopLevelObject> historyResponse(
        HttpServletRequest request, ObjectHistory objectHistory)
    {
        return Optional.ofNullable(objectHistory)
            .map(RdapHistory::new)
            .map(history -> responseMaker.makeResponse(history, request))
            .map(response -> new ResponseEntity<TopLevelObject>(
                    response, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> historyResponse(
        HttpServletRequest request, List<ObjectHistory> histories)
    {
        return Optional.ofNullable(histories.size() > 0 ? histories : null)
            .map(RdapHistory::new)
            .map(history -> responseMaker.makeResponse(history, request))
            .map(response -> new ResponseEntity<TopLevelObject>(
                    response, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> singleObjectResponse(
        HttpServletRequest request, RdapObject rdapObject)
    {
        return Optional.ofNullable(rdapObject)
            .map(rObject -> responseMaker.makeResponse(rObject, request))
            .map(rdapTLO -> new ResponseEntity<TopLevelObject>(
                rdapTLO, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> searchResponse(
        HttpServletRequest request, ObjectClass objectClass,
        Stream<RdapObject> rdapObjectStream)
    {
        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(
                RdapSearch.build(objectClass,
                                 rdapObjectStream.collect(Collectors.toList())),
                                 request),
                responseHeaders, HttpStatus.OK);
    }

    private void setupResponseHeaders()
    {
        responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(RdapConstants.RDAP_MEDIA_TYPE);
    }
}
