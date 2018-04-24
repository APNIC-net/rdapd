package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RdapDomainsSearch
    extends RdapSearch
{
    RdapDomainsSearch(List<RdapObject> rdapObjects)
    {
        super(rdapObjects);
    }

    @Override
    @JsonProperty(value="domainSearchResults")
    public List<RdapObject> getRdapObjects()
    {
        return super.getRdapObjects();
    }
}
