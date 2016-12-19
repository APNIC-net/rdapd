package net.apnic.whowas.rdap;

import java.util.List;

public class Notice {
    private final String title;
    private final List<String> description;
    private final List<Link> links;

    public Notice(String title, List<String> description) {
        this(title, description, null);
    }

    public Notice(String title, List<String> description, List<Link> links) {
        this.title = title;
        this.description = description;
        this.links = links;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<Link> getLinks() {
        return links;
    }
}
