package net.apnic.whowas.ip;

import net.apnic.whowas.rdap.IpNetwork;
import net.apnic.whowas.types.IpInterval;

import java.util.Optional;

public interface IpService {
    Optional<IpNetwork> find(IpInterval range);
}
