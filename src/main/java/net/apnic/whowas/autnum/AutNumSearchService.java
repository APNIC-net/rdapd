package net.apnic.whowas.autnum;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.rdap.AutNum;

import java.util.Optional;

public interface AutNumSearchService {
    Optional<AutNum> findCurrent(ASN asn);

    Optional<ObjectHistory> findHistory(ASN asn);
}
