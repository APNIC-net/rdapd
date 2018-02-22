package net.apnic.rdapd.ip;

import net.apnic.rdapd.rdap.IpNetwork;
import net.apnic.rdapd.types.IpInterval;

import java.util.Optional;

public interface IpService {
    Optional<IpNetwork> find(IpInterval range);
}
