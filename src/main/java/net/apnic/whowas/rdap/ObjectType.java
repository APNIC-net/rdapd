package net.apnic.whowas.rdap;

public enum ObjectType
{
    AUTNUM("autnum"),
    DOMAIN("domain"),
    ENTITY("entity"),
    IP("ip"),
    NAMESERVER("nameserver");

    private final String pathSegment;

    ObjectType(String pathSegment)
    {
        this.pathSegment = pathSegment;
    }

    public String getPathSegment()
    {
        return pathSegment;
    }
}
