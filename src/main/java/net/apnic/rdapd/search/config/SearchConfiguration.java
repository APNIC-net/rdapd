package net.apnic.rdapd.search.config;

import java.util.List;

import net.apnic.rdapd.history.ObjectSearchIndex;
import net.apnic.rdapd.search.SearchEngine;
import net.apnic.rdapd.search.SearchIndex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="rdap")
public class SearchConfiguration
{
    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MIN_SEARCH_LIMIT = 1;

    private int searchLimit = DEFAULT_SEARCH_LIMIT;

    public int getSearchLimit()
    {
        return searchLimit;
    }

    @Autowired
    @Bean
    public SearchEngine searchEngine(List<SearchIndex> searchIndexes)
    {
        return new SearchEngine(searchIndexes, getSearchLimit());
    }

    public void setSearchLimit(int searchLimit)
    {
        if(searchLimit < MIN_SEARCH_LIMIT)
        {
            throw new IndexOutOfBoundsException(
                "SearchLimit can not be less than " + MIN_SEARCH_LIMIT);
        }
        this.searchLimit = searchLimit;
    }

    @Autowired
    @Bean
    public ObjectSearchIndex objectSearchIndex(SearchEngine searchEngine)
    {
        return searchEngine::getObjectsForKey;
    }
}
