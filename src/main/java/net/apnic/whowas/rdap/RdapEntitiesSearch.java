package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RdapEntitiesSearch
    extends RdapSearch
{
    RdapEntitiesSearch(List<RdapObject> rdapObjects)
    {
        super(rdapObjects);
    }

    @Override
    @JsonProperty(value="entitySearchResults")
    public List<RdapObject> getRdapObjects()
    {
        return super.getRdapObjects();
    }
}
