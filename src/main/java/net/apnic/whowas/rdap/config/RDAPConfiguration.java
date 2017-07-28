package net.apnic.whowas.rdap.config;

import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class RDAPConfiguration
{
    @Autowired
    @Bean
    public RDAPControllerUtil rdapControllerUtil(ObjectIndex objectIndex)
    {
        return new RDAPControllerUtil(objectIndex);
    }
}
