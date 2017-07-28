package net.apnic.whowas.rdap.controller;

import net.apnic.whowas.rdap.TopLevelObject;

public class RDAPResponseMaker
{
    public static TopLevelObject makeResponse(Object object)
    {
        return TopLevelObject.of(object, null);
    }
}
