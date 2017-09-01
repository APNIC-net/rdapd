package net.apnic.whowas.rdap.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.rdap.Error;
import net.apnic.whowas.rdap.http.RdapConstants;
import net.apnic.whowas.rdap.RdapHistory;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RDAPControllerUtil
{
    private IntervalTree<IP, ObjectHistory, IpInterval> intervalTree;
    private ObjectIndex objectIndex = null;
    private HttpHeaders responseHeaders = null;
    private RDAPResponseMaker responseMaker = null;

    public RDAPControllerUtil(ObjectIndex objectIndex,
        IntervalTree<IP, ObjectHistory, IpInterval> intervalTree,
        RDAPResponseMaker responseMaker)
    {
        setupResponseHeaders();
        this.objectIndex = objectIndex;
        this.intervalTree = intervalTree;
        this.responseMaker = responseMaker;
    }

    public ObjectIndex getObjectIndex()
    {
        return objectIndex;
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
        return getObjectIndex().historyForObject(objectKey)
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
        return getObjectIndex().historyForObject(objectKey)
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
        HttpServletRequest request, IpInterval range)
    {
        return intervalTree.intersecting(range)
            .filter(t -> t.snd().mostCurrent().isPresent())
            .reduce((a, b) -> a.fst().compareTo(b.fst()) <= 0 ? b : a)
            .flatMap(t -> t.snd().mostCurrent())
            .map(Revision::getContents)
            .map(rdapObject -> responseMaker.makeResponse(rdapObject, request))
            .map(rdapTLO -> new ResponseEntity<TopLevelObject>(
                rdapTLO, responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(
                responseMaker.makeResponse(Error.NOT_FOUND, request),
                responseHeaders,
                HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Void> mostCurrentResponseHead(
        HttpServletRequest request, ObjectKey objectKey)
    {
        return getObjectIndex().historyForObject(objectKey)
            .flatMap(ObjectHistory::mostCurrent)
            .map(rev -> new ResponseEntity<Void>(responseHeaders, HttpStatus.OK))
            .orElse(new ResponseEntity<Void>(responseHeaders, HttpStatus.NOT_FOUND));
    }

    private void setupResponseHeaders()
    {
        responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(RdapConstants.RDAP_MEDIA_TYPE);
    }
}
