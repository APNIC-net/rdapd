package net.apnic.whowas.search;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SearchEngine
{
    private HashMap<ObjectClass, HashMap<String, SearchIndex>> indexes = new HashMap<>();
    private int searchLimit = 0;

    public SearchEngine(List<SearchIndex> searchIndexes, int searchLimit)
    {
        this.searchLimit = searchLimit;
        buildIndexMap(searchIndexes);
    }

    public void putIndexEntry(Revision revision, ObjectKey objectKey)
    {
        Optional.ofNullable(indexes.get(objectKey.getObjectClass()))
            .ifPresent(cIndex -> cIndex.forEach((k, v) -> v.putMapping(revision, objectKey)));
    }

    private void buildIndexMap(List<SearchIndex> searchIndexes)
    {
        for(SearchIndex si : searchIndexes)
        {
            if(indexes.containsKey(si.getIndexClass()) == false)
            {
                indexes.put(si.getIndexClass(), new HashMap<String, SearchIndex>());
            }
            indexes.get(si.getIndexClass()).put(si.getIndexAttribute(), si);
        }
    }

    public void commit()
    {
        indexes.forEach((ignore1, value) ->
        {
            value.forEach((ignore2, index) -> index.commit());
        });
    }

    public Stream<ObjectKey> getObjectsForKey(ObjectSearchKey objectSearchKey)
    {
        return Optional.ofNullable(indexes.get(objectSearchKey.getObjectClass()))
            .map(cIndex -> cIndex.get(objectSearchKey.getAttribute()))
            .map(sIndex -> sIndex.getObjectsForKey(objectSearchKey, searchLimit))
            .orElseGet(() -> Stream.empty());
    }
}
