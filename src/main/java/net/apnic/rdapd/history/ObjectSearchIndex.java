package net.apnic.rdapd.history;

import net.apnic.rdapd.search.SearchResponse;

public interface ObjectSearchIndex
{
    SearchResponse historySearchForObject(ObjectSearchKey searchKey);
}
