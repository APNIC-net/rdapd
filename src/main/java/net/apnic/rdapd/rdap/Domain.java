package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.apnic.rdapd.history.ObjectKey;

/**
 * Domain RDAP object.
 */
public class Domain
    extends GenericObject
{
    /**
     * Child object represents a name server for a Domain object.
     */
    public static class NameServer
        implements Serializable
    {
        private String ldhName;

        public NameServer(String ldhName)
        {
            this.ldhName = ldhName;
        }

        public String getLdhName()
        {
            return ldhName;
        }

        public String getObjectClassName()
        {
            return "nameserver";
        }
    }

    private ArrayList<NameServer> nameServers = new ArrayList<NameServer>();

    /**
     * Constructs a domain object with the given key.
     */
    public Domain(ObjectKey objectKey)
    {
        super(objectKey);
    }

    /**
     * Adds a new name server to this Domain object.
     *
     * @param ldhName The ldh name of the nameserver
     */
    public void addNameServer(String ldhName)
    {
        nameServers.add(new Domain.NameServer(ldhName));
    }

    /**
     * Gets the domains ldh name
     *
     * @return ldh name for the domain
     */
    public String getLdhName()
    {
        return getObjectKey().getObjectName();
    }

    /**
     * Provides a list of name servers added to this Domain object.
     *
     * @return List of NameServers
     */
    @JsonProperty("nameservers")
    public List<NameServer> getNameServers()
    {
        return nameServers;
    }

    /**
     * {@inheritDocs}
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.DOMAIN;
    }

    /**
     * {@inheritDocs}
     */
    @Override
    public String getPathHandle()
    {
        return getHandle();
    }

    /**
     * {@inheritDocs}
     */
    public String toString()
    {
        return String.format("domain: %s", getObjectKey().getObjectName());
    }
}
