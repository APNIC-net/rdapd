package net.apnic.whowas.rdap.config;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.Link;
import net.apnic.whowas.rdap.Notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General configuration bootstrap class that build and passes information
 * recieved through config files.
 */
@Configuration
@ConfigurationProperties(prefix="rdap")
public class RDAPConfiguration
{
    private static final Notice TRUNCATED_NOTICE;

    static
    {
        TRUNCATED_NOTICE = new Notice(
            "Search Policy",
            "result set truncated due to limits",
            Arrays.asList("Search results limited"), null);
    }

    private List<ConfigNotice> configNotices = null;
    private List<Notice> defaultNotices = null;
    private String defaultPort43 = null;

    /**
     * Config class represents a link object in this applications configuration
     * file.
     */
    public static class ConfigLink
    {
        private String href;
        private String rel;
        private String type;

        public String getHref()
        {
            return href;
        }

        public String getRel()
        {
            return rel;
        }

        public String getType()
        {
            return type;
        }

        public void setHref(String href)
        {
            this.href = href;
        }

        public void setRel(String rel)
        {
            this.rel = rel;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public Link toLink()
        {
            return new Link(null, getRel(), getHref(), getType());
        }
    }

    /**
     * Config class represents a notice object in this applcations configuration
     * file.
     */
    public static class ConfigNotice
    {
        private List<String> description = new ArrayList<String>();
        private List<ConfigLink> links = new ArrayList<ConfigLink>();
        private String title;

        public List<String> getDescription()
        {
            return description;
        }

        public List<ConfigLink> getLinks()
        {
            return links;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public Notice toNotice()
        {
            return new Notice(getTitle(), getDescription(),
                getLinks().stream().map(cLink -> cLink.toLink())
                .collect(Collectors.toList()));
        }
    }

    /**
     * Constructs a set of default notices provided with every rdap response.
     */
    private void configDefaultNotices()
    {
        defaultNotices = Collections.unmodifiableList(
            Optional.ofNullable(configNotices)
            .map(cn -> cn.stream().map(cNotice -> cNotice.toNotice())
                    .collect(Collectors.toList()))
            .orElse(Collections.emptyList()));

        configNotices = null;
    }

    /**
     * Bean for a list of default RDAP notices.
     */
    @Bean
    public List<Notice> defaultNotices()
    {
        if(defaultNotices == null)
        {
            configDefaultNotices();
        }
        return defaultNotices;
    }

    public List<ConfigNotice> getNotices()
    {
        return configNotices;
    }

    public String getPort43()
    {
        return defaultPort43 == null || defaultPort43.isEmpty() ? null : defaultPort43;
    }

    @Autowired
    @Bean
    public RDAPResponseMaker rdapResponseMaker(List<Notice> defaultNotices)
    {
        return new RDAPResponseMaker(defaultNotices, TRUNCATED_NOTICE, getPort43());
    }

    public void setNotices(List<ConfigNotice> notices)
    {
        this.configNotices = notices;
    }

    public void setPort43(String port43)
    {
        this.defaultPort43 = port43;
    }
}
