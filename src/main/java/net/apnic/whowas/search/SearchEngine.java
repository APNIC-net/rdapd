package net.apnic.whowas.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;

public class SearchEngine
{
    private Map<ObjectClass, Map<String, List<SearchIndex>>> indexes = new HashMap<>();
    private int searchLimit = 0;

    public SearchEngine(List<SearchIndex> searchIndexes, int searchLimit)
    {
        this.searchLimit = searchLimit;
        buildIndexMap(searchIndexes);
    }

    public void putIndexEntry(Revision revision, ObjectKey objectKey)
    {
        Optional.ofNullable(indexes.get(objectKey.getObjectClass()))
            .ifPresent(cIndex -> cIndex.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(v -> v.stream())
                .forEach(si -> si.putMapping(revision, objectKey)));
    }

    private void buildIndexMap(List<SearchIndex> searchIndexes)
    {
        for(SearchIndex si : searchIndexes)
        {
            if(indexes.containsKey(si.getIndexClass()) == false)
            {
                indexes.put(si.getIndexClass(), new HashMap<String, List<SearchIndex>>());
            }
            Map<String, List<SearchIndex>> fieldIndexesMap =
                indexes.get(si.getIndexClass());
            if (!fieldIndexesMap.containsKey(si.getIndexAttribute())) {
                fieldIndexesMap.put(si.getIndexAttribute(),
                                    new ArrayList<SearchIndex>());
            }
            List<SearchIndex> fieldIndexes =
                fieldIndexesMap.get(si.getIndexAttribute());
            fieldIndexes.add(si);
        }
    }

    public void commit()
    {
        indexes.entrySet().stream()
            .map(Map.Entry::getValue)
            .flatMap(v -> v.entrySet().stream())
            .map(Map.Entry::getValue)
            .flatMap(v -> v.stream())
            .forEach(si -> si.commit());
    }

    public SearchResponse getObjectsForKey(ObjectSearchKey objectSearchKey)
    {
        List<SearchIndex> fieldIndexes =
            Optional.ofNullable(indexes.get(objectSearchKey.getObjectClass()))
                .map(cIndex -> cIndex.get(objectSearchKey.getAttribute()))
                .orElse(null);
        if (fieldIndexes == null || fieldIndexes.isEmpty()) {
            return SearchResponse.makeEmpty();
        }
        SearchIndex applicableIndex = fieldIndexes.stream()
            .filter(si -> si.supportsSearchType(objectSearchKey.getObjectSearchType()))
            .findFirst()
            .orElse(null);
        if (applicableIndex == null) {
            return SearchResponse.makeEmpty();
        }
        return applicableIndex.getObjectsForKey(objectSearchKey, searchLimit);
    }
}
