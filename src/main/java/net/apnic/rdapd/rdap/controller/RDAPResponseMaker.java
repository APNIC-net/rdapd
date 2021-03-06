package net.apnic.rdapd.rdap.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import net.apnic.rdapd.rdap.Notice;
import net.apnic.rdapd.rdap.TopLevelObject;

public class RDAPResponseMaker
{
    private final List<Notice> defaultNotices;
    private final String defaultPort43;
    private final Notice defaultTruncatedNotice;

    public RDAPResponseMaker()
    {
        this(Collections.emptyList(), null, null);
    }

    public RDAPResponseMaker(List<Notice> defaultNotices,
        Notice defaultTruncatedNotice,
        String defaultPort43)
    {
        this.defaultNotices = defaultNotices;
        this.defaultPort43 = defaultPort43;
        this.defaultTruncatedNotice  = defaultTruncatedNotice;
    }

    public TopLevelObject makeResponse(Object object, HttpServletRequest request)
    {
        return TopLevelObject.of(object,
            defaultNotices, defaultPort43);
    }

    public TopLevelObject makeTruncatedResponse(Object object,
        HttpServletRequest request)
    {
        return TopLevelObject.of(object,
            Stream.concat(defaultNotices.stream(), Stream.of(defaultTruncatedNotice))
            .collect(Collectors.toList()),
            defaultPort43);
    }
}
