package net.apnic.whowas.domain.config;

import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.rdap.Domain;
import net.apnic.whowas.search.WildCardSearchIndex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration
{
    @Bean
    public WildCardSearchIndex domainNameSearchIndex()
    {
        return new WildCardSearchIndex(ObjectClass.DOMAIN, "name",
            (rev, objectKey) ->
            {
                return Stream.of(((Domain)rev.getContents()).getLdhName());
            });
    }
}
