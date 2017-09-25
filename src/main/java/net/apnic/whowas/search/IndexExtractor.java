package net.apnic.whowas.search;

import net.apnic.whowas.history.Revision;
import net.apnic.whowas.history.ObjectKey;

public interface IndexExtractor
{
    String extract(Revision revision, ObjectKey objectKey);
}
