package net.apnic.rdapd.rdap;

import java.util.List;

import net.apnic.rdapd.history.ObjectClass;

public class RdapSearch
{
    protected final List<RdapObject> rdapObjects;

    protected RdapSearch(List<RdapObject> rdapObjects)
    {
        this.rdapObjects = rdapObjects;
    }

    public List<RdapObject> getRdapObjects()
    {
        return rdapObjects;
    }

    public static RdapSearch build(ObjectClass objectClass,
                                   List<RdapObject> rdapObjects)
    {
        switch(objectClass)
        {
        case ENTITY:
            return new RdapEntitiesSearch(rdapObjects);
        case DOMAIN:
            return new RdapDomainsSearch(rdapObjects);
        default:
            return null;
        }
    }
}
