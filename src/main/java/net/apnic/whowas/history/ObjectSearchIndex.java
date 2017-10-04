package net.apnic.whowas.history;

import java.util.stream.Stream;

public interface ObjectSearchIndex
{
    Stream<ObjectKey> historySearchForObject(ObjectSearchKey searchKey);
}
