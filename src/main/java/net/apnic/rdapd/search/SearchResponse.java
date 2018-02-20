package net.apnic.rdapd.search;

import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectKey;

/**
 * Generated response from performing a seach against a search index.
 */
public class SearchResponse
{
    private final Stream<ObjectKey> keys;
    private final boolean truncated;

    private SearchResponse(Stream<ObjectKey> keys, boolean truncated)
    {
        this.keys = keys;
        this.truncated = truncated;
    }

    public Stream<ObjectKey> getKeys()
    {
        return keys;
    }

    public boolean isTruncated()
    {
        return truncated;
    }

    public static SearchResponse makeEmpty()
    {
        return make(Stream.empty(), false);
    }

    public static SearchResponse make(Stream<ObjectKey> keys, boolean truncated)
    {
        return new SearchResponse(keys, truncated);
    }
}
