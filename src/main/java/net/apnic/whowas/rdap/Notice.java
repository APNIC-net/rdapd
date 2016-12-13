package net.apnic.whowas.rdap;

import java.util.List;

class Notice {
    private final String title;
    private final List<String> description;

    public Notice(String title, List<String> description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDescription() {
        return description;
    }
}
