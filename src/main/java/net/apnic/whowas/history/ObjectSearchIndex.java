package net.apnic.whowas.history;

import net.apnic.whowas.search.SearchResponse;

public interface ObjectSearchIndex
{
    SearchResponse historySearchForObject(ObjectSearchKey searchKey);
}
