package net.apnic.whowas.search.config;

import java.util.List;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.rdap.Entity;
import net.apnic.whowas.search.SearchEngine;
import net.apnic.whowas.search.SearchIndex;
import net.apnic.whowas.search.WildCardSearchIndex;

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

    @Bean
    public WildCardSearchIndex entityHandleSearchindex()
    {
        return new WildCardSearchIndex(ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));
    }

    @Bean
    public WildCardSearchIndex entityFNSesearchindex()
    {
        return new WildCardSearchIndex(ObjectClass.ENTITY, "fn",
            (rev, objectKey) ->
            {
                return ((Entity)rev.getContents())
                    .getVCard()
                    .findVCardAttribute("fn")
                    .map(vcard ->
                    {
                        return vcard.getValue().toString();
                    });
            });
    }

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
