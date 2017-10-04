package net.apnic.whowas.search;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;

import java.util.stream.Stream;

public interface SearchIndex
{
    String getIndexAttribute();

    ObjectClass getIndexClass();

    Stream<ObjectKey> getObjectsForKey(ObjectSearchKey objectSearchKey);

    void putMapping(Revision revision, ObjectKey objectKey);
}
