package net.apnic.rdapd.history;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Look up object histories by ObjectKey.
 */
public interface ObjectIndex
{
    /**
     * Provides the history for a single ObjectKey.
     *
     * @param objectKey ObjectKey to recieve History for.
     * @return
     */
    Optional<ObjectHistory> historyForObject(ObjectKey objectKey);

    Stream<ObjectHistory> historyForObject(Stream<ObjectKey> objectKeys);
}
