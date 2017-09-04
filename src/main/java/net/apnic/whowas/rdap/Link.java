package net.apnic.whowas.rdap;

public class Link {
    private final String value;
    private final String rel;
    private final String href;
    private final String type;

    public Link(String value, String rel, String href, String type) {
        this.value = value;
        this.rel = rel;
        this.href = href;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public Link withValue(String context)
    {
        return new Link(context, rel, href, type);
    }
}
