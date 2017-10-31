package net.apnic.whowas.rdap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class represents a single RDAP Notice object.
 *
 * @see RFC7483
 */
public class Notice
{
    private final String title;
    private final List<String> description;
    private final List<Link> links;
    private final String type;

    public Notice(String title, List<String> description, List<Link> links)
    {
        this(title, null, description, links);
    }

    /**
     * Constructs a new Notice object
     */
    public Notice(String title, String type, List<String> description,
                  List<Link> links)
    {
        this.title = title;
        this.type = type;
        this.description = Collections.unmodifiableList(
            new ArrayList<>(Optional.ofNullable(description)
                                    .orElse(Collections.emptyList())));
        this.links = Collections.unmodifiableList(
            new ArrayList<>(Optional.ofNullable(links)
                                    .orElse(Collections.emptyList())));
    }

    /**
     * Returns the notice title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the notice type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the list of String used for this Notice description
     */
    public List<String> getDescription()
    {
        return description;
    }

    /**
     * Returns the list of Link objects used in this Notice
     */
    public List<Link> getLinks()
    {
        return links;
    }

    public Notice withContext(String context)
    {
        return new Notice(title, type, description,
            links.stream().map(link -> link.withValue(context))
            .collect(Collectors.toList()));
    }
}
