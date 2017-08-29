package net.apnic.whowas.rdap.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.rdap.Notice;
import net.apnic.whowas.rdap.TopLevelObject;

public class RDAPResponseMaker
{
    private List<Notice> defaultNotices;

    public RDAPResponseMaker()
    {
        this(Collections.emptyList());
    }

    public RDAPResponseMaker(List<Notice> defaultNotices)
    {
        this.defaultNotices = defaultNotices;
    }


    public TopLevelObject makeResponse(Object object)
    {
        return makeResponse(object, "");
    }

    public TopLevelObject makeResponse(Object object,
        HttpServletRequest request)
    {
        return makeResponse(object, request.getRequestURL().toString());
    }

    public TopLevelObject makeResponse(Object object, String context)
    {
        return TopLevelObject.of(object,
            defaultNotices.stream()
            .map(notice -> notice.withContext(context))
            .collect(Collectors.toList()));
    }
}
