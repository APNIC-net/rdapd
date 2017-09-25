package net.apnic.whowas.search.config;

import java.util.List;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectSearchIndex;
import net.apnic.whowas.search.BasicSearchIndex;
import net.apnic.whowas.search.SearchEngine;
import net.apnic.whowas.search.SearchIndex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration
{
    @Bean
    public BasicSearchIndex entityHandleSearchindex()
    {
        return new BasicSearchIndex(ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> objectKey.getObjectName());
    }

    @Bean
    public BasicSearchIndex entityFNSesearchindex()
    {
        return new BasicSearchIndex(ObjectClass.ENTITY, "fn",
            (rev, objectKey) ->
            {
                /*if(rev.getContents() instanceof GenericObject)
                {
                    ((GenericObject)rev.getContents()).getRpsl();
                }*/
                /*if(rev.getContents() instanceof GenericObject)
                {
                    //((GenericObject)rev.getContents()).getRpsl();//.getPrimaryAttribute();
                }*/
                return "";
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
