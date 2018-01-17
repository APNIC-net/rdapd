package net.apnic.whowas.rdap;

public class Link
{
    private final String rel;
    private final String href;
    private final String type;

    public Link(String rel, String href, String type) {
        this.rel = rel;
        this.href = href;
        this.type = type;
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
}
