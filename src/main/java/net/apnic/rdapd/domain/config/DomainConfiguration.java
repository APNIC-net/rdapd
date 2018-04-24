package net.apnic.rdapd.domain.config;

import java.util.stream.Stream;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.rdap.Domain;
import net.apnic.rdapd.search.WildCardSearchIndex;

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
