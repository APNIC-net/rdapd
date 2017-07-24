package net.apnic.whowas.rdap.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.rdap.TopLevelObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RDAPControllerUtil
{
    private ObjectIndex objectIndex = null;

    public RDAPControllerUtil(ObjectIndex objectIndex)
    {
        this.objectIndex = objectIndex;
    }

    public ObjectIndex getObjectIndex()
    {
        return objectIndex;
    }

    public ResponseEntity<TopLevelObject> mostRecentResponse(
        HttpServletRequest request, ObjectKey objectKey)
    {
        return getObjectIndex().historyForObject(objectKey)
            .flatMap(ObjectHistory::mostRecent)
            .map(Revision::getContents)
            .map(rdapObject -> RDAPResponseMaker.makeResponse(rdapObject))
            .map(rdapTLO -> new ResponseEntity<TopLevelObject>(rdapTLO, HttpStatus.OK))
            .orElse(new ResponseEntity<TopLevelObject>(HttpStatus.NOT_FOUND));
    }
}
