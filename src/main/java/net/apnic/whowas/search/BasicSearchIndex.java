package net.apnic.whowas.search;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

public class BasicSearchIndex
    implements SearchIndex
{
    private final IndexExtractor extractor;
    private HashMap<String, ObjectKey> index = null;
    private final String indexAttribute;
    private final ObjectClass indexClass;

    public BasicSearchIndex(ObjectClass indexClass, String indexAttribute,
                            IndexExtractor extractor)
    {
        this.extractor = extractor;
        this.index = new HashMap<String, ObjectKey>();
        this.indexAttribute = indexAttribute;
        this.indexClass = indexClass;
    }

    @Override
    public String getIndexAttribute()
    {
        return indexAttribute;
    }

    @Override
    public ObjectClass getIndexClass()
    {
        return indexClass;
    }

    @Override
    public Stream<ObjectKey> getObjectsForKey(ObjectSearchKey objectSearchKey)
    {
        if(index.containsKey(objectSearchKey.getObjectName()))
        {
            return Arrays.asList(index.get(objectSearchKey.getObjectName())).stream();
        }
        return Stream.empty();
    }

    @Override
    public void putMapping(Revision revision, ObjectKey objectKey)
    {
        //index.put(objectKey.getObjectName(), objectKey);
        index.put(extractor.extract(revision, objectKey), objectKey);
    }
}
