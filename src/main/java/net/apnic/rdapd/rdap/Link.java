package net.apnic.rdapd.rdap;

public class Link
{
    private final String rel;
    private final String href;
    private final String type;
    private final String hreflang;

    public Link(String rel, String href, String type) {
        this.rel = rel;
        this.href = href;
        this.type = type;
        this.hreflang = null;
    }

    public Link(String rel, String href, String type, String hreflang) {
        this.rel = rel;
        this.href = href;
        this.type = type;
        this.hreflang = hreflang;
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

    public String getHreflang() {
        return hreflang;
    }
}
