package net.apnic.rdapd.rdap;

public enum ObjectType
{
    AUTNUM("autnum", "autnum"),
    DOMAIN("domain", "domain"),
    ENTITY("entity", "entity"),
    IP("ip network", "ip"),
    NAMESERVER("nameserver", "nameserver");

    private final String className;
    private final String pathSegment;

    ObjectType(String className, String pathSegment)
    {
        this.className = className;
        this.pathSegment = pathSegment;
    }

    public String getClassName()
    {
        return className;
    }

    public String getPathSegment()
    {
        return pathSegment;
    }
}
