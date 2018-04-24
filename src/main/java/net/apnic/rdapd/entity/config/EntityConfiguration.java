package net.apnic.rdapd.entity.config;

import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.rdap.Entity;
import net.apnic.rdapd.search.WildCardSearchIndex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityConfiguration
{
    @Bean
    public WildCardSearchIndex entityHandleSearchIndex()
    {
        return new WildCardSearchIndex(ObjectClass.ENTITY, "handle",
            (rev, objectKey) -> Stream.of(objectKey.getObjectName()));
    }

    @Bean
    public WildCardSearchIndex entityFNSearchIndex()
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
}
