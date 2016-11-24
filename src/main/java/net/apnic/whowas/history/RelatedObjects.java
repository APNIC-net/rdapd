package net.apnic.whowas.history;

import java.util.Optional;

/**
 * Look up related objects by ObjectKey.
 */
public interface RelatedObjects {
    Optional<ObjectHistory> historyForObject(ObjectKey objectKey);
}
