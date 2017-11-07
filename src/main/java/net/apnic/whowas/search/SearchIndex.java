package net.apnic.whowas.search;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.ObjectSearchType;
import net.apnic.whowas.history.Revision;

/**
 *
 */
public interface SearchIndex
{
    default void commit() {};

    String getIndexAttribute();

    ObjectClass getIndexClass();

    boolean supportsSearchType(ObjectSearchType objectSearchType);

    SearchResponse getObjectsForKey(ObjectSearchKey objectSearchKey,
                                    int limit);

    void putMapping(Revision revision, ObjectKey objectKey);
}
