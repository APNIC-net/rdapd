package net.apnic.whowas.history;

import java.util.Optional;

/**
 * Look up object histories by ObjectKey.
 */
public interface ObjectIndex {
    Optional<ObjectHistory> historyForObject(ObjectKey objectKey);
}
