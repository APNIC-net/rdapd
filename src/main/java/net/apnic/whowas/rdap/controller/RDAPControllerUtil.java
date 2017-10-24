package net.apnic.whowas.rdap.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.rdap.Error;
import net.apnic.whowas.rdap.http.RdapConstants;
import net.apnic.whowas.rdap.RdapHistory;
import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.rdap.RdapSearch;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * A set of helper functions and utilities to make RDAP results for controllers.
 */
@Component
public class RDAPControllerUtil
{
    private final IntervalTree<IP, ObjectHistory, IpInterval> intervalTree;
    private final ObjectIndex objectIndex;
    private final ObjectSearchIndex searchIndex;
    private HttpHeaders responseHeaders = null;
    private final RDAPResponseMaker responseMaker;

    /**
     * Default constructor.
     *
     * Requires a set of global scope beans to accomplish util functions of this
     * class.
     */
    @Autowired
    public RDAPControllerUtil(ObjectIndex objectIndex,
        ObjectSearchIndex searchIndex,
        IntervalTree<IP, ObjectHistory, IpInterval> intervalTree,
        RDAPResponseMaker responseMaker)
    {
        setupResponseHeaders();
        this.objectIndex = objectIndex;
        this.intervalTree = intervalTree;
        this.responseMaker = responseMaker;
        this.searchIndex = searchIndex;
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
        HttpServletRequest request, ObjectKey objectKey)
    {
        return objectIndex.historyForObject(objectKey)
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
        HttpServletRequest request, IpInterval range)
    {
        int pfxCap = range.prefixSize() +
            (range.low().getAddressFamily() == IP.AddressFamily.IPv4 ? 8 : 16);

        List<ObjectHistory> ipHistory =
            intervalTree
                .intersecting(range)
                .filter(t -> t.first().prefixSize() <= pfxCap)
                .sorted(Comparator.comparing(Tuple::first))
                .map(Tuple::second)
                .collect(Collectors.toList());

        return Optional.ofNullable(ipHistory.size() > 0 ? ipHistory : null)
            .map(RdapHistory::new)
            .map(history -> responseMaker.makeResponse(history, request))
            .map(response -> new ResponseEntity<TopLevelObject>(
                    response, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> mostCurrentResponseGet(
        HttpServletRequest request, ObjectKey objectKey)
    {
        return objectIndex.historyForObject(objectKey)
            .flatMap(ObjectHistory::mostCurrent)
            .map(Revision::getContents)
            .map(rdapObject -> responseMaker.makeResponse(rdapObject, request))
            .map(rdapTLO -> new ResponseEntity<TopLevelObject>(
                rdapTLO, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<TopLevelObject> mostCurrentResponseGet(
        HttpServletRequest request, ObjectSearchKey objectSearchKey)
    {
        List<RdapObject> searchObjects = objectIndex.historyForObject(
            searchIndex.historySearchForObject(objectSearchKey))
            .filter(oHistory -> oHistory.mostCurrent().isPresent())
            .map(oHistory -> oHistory.mostCurrent().get().getContents())
            .collect(Collectors.toList());

        return new ResponseEntity<TopLevelObject>(
            responseMaker.makeResponse(
                RdapSearch.build(objectSearchKey.getObjectClass(),
                                 searchObjects), request),
                responseHeaders, HttpStatus.OK);
    }

    public ResponseEntity<TopLevelObject> mostCurrentResponseGet(
        HttpServletRequest request, IpInterval range)
    {
        return intervalTree.equalToAndLeastSpecific(range)
            .filter(t -> t.second().mostCurrent().isPresent())
            .reduce((a, b) -> a.first().compareTo(b.first()) <= 0 ? b : a)
            .flatMap(t -> t.second().mostCurrent())
            .map(Revision::getContents)
            .map(rdapObject -> responseMaker.makeResponse(rdapObject, request))
            .map(rdapTLO -> new ResponseEntity<TopLevelObject>(
                rdapTLO, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    private void setupResponseHeaders()
    {
        responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(RdapConstants.RDAP_MEDIA_TYPE);
    }
}
