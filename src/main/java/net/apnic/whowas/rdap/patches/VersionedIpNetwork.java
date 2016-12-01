package net.apnic.whowas.rdap.patches;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.*;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.net.InetAddress;
import java.util.List;

public class VersionedIpNetwork {
    private final IPNetwork ipNetwork;
    private final String ipVersion;

    public VersionedIpNetwork(IPNetwork ipNetwork, String ipVersion) {
        this.ipNetwork = ipNetwork;
        this.ipVersion = ipVersion;
    }

    public VersionedIpNetwork(
            List<Link> links,
            List<Notice> notices,
            List<Notice> remarks,
            String lang,
            List<Event> events,
            List<Status> status,
            DomainName port43,
            String handle,
            InetAddress startAddress,
            InetAddress endAddress,
            String ipVersion,
            String name,
            String type,
            String country,
            String parentHandle,
            List<Entity> entities
    ) {
        this(new IPNetwork(links, notices, remarks, lang, IPNetwork.OBJECT_CLASS_NAME,
                events, status, port43, handle, startAddress, endAddress, name, type,
                country, parentHandle, entities), ipVersion);
    }

    @JsonUnwrapped
    public IPNetwork getIpNetwork() {
        return ipNetwork;
    }

    public String getIpVersion() {
        return ipVersion;
    }
}