package net.apnic.rdapd.search;

import java.util.stream.Stream;

import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.history.ObjectKey;

public interface IndexExtractor<T>
{
    Stream<T> extract(Revision revision, ObjectKey objectKey);
}
