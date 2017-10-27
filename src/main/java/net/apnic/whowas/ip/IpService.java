package net.apnic.whowas.ip;

import net.apnic.whowas.rdap.RdapObject;
import net.apnic.whowas.types.IpInterval;

import java.util.Optional;

public interface IpService {
    Optional<RdapObject> find(IpInterval range);
}
