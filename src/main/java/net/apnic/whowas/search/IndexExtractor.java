package net.apnic.whowas.search;

import java.util.stream.Stream;

import net.apnic.whowas.history.Revision;
import net.apnic.whowas.history.ObjectKey;

public interface IndexExtractor<T>
{
    Stream<T> extract(Revision revision, ObjectKey objectKey);
}
