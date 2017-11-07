package net.apnic.whowas.domain.config;

import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.rdap.Domain;
import net.apnic.whowas.search.WildCardSearchIndex;
import net.apnic.whowas.search.RegexSearchIndex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration
{
    @Bean
    public WildCardSearchIndex domainNameWildCardSearchIndex()
    {
        return new WildCardSearchIndex(ObjectClass.DOMAIN, "name",
            (rev, objectKey) ->
            {
                return Stream.of(((Domain)rev.getContents()).getLdhName());
            });
    }

    @Bean
    public RegexSearchIndex domainNameRegexSearchIndex()
    {
        return new RegexSearchIndex(ObjectClass.DOMAIN, "name",
            (rev, objectKey) ->
            {
                return Stream.of(((Domain)rev.getContents()).getLdhName());
            });
    }
}
