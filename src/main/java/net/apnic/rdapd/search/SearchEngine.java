package net.apnic.rdapd.search;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.ObjectSearchKey;
import net.apnic.rdapd.history.Revision;

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

    public SearchResponse getObjectsForKey(ObjectSearchKey objectSearchKey)
    {
        return Optional.ofNullable(indexes.get(objectSearchKey.getObjectClass()))
            .map(cIndex -> cIndex.get(objectSearchKey.getAttribute()))
            .map(sIndex -> sIndex.getObjectsForKey(objectSearchKey, searchLimit))
            .orElseGet(() -> SearchResponse.makeEmpty());
    }
}
