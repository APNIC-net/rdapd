package net.apnic.rdapd.autnum;

import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.rdap.AutNum;

import java.util.Optional;

public interface AutNumSearchService {
    Optional<AutNum> findCurrent(ASN asn);

    Optional<ObjectHistory> findHistory(ASN asn);
}
