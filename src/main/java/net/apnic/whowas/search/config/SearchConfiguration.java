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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration
{
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

    @Autowired
    @Bean
    public SearchEngine searchEngine(List<SearchIndex> searchIndexes)
    {
        return new SearchEngine(searchIndexes);
    }

    @Autowired
    @Bean
    public ObjectSearchIndex objectSearchIndex(SearchEngine searchEngine)
    {
        return searchEngine::getObjectsForKey;
    }
}
