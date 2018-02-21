package net.apnic.rdapd.search;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.ObjectSearchKey;
import net.apnic.rdapd.history.Revision;

/**
 *
 */
public interface SearchIndex
{
    default void commit() {};

    String getIndexAttribute();

    ObjectClass getIndexClass();

    SearchResponse getObjectsForKey(ObjectSearchKey objectSearchKey,
                                    int limit);

    void putMapping(Revision revision, ObjectKey objectKey);
}
