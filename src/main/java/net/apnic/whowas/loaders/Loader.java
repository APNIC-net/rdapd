package net.apnic.whowas.loaders;

import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;

public interface Loader {
    IntervalTree<IP, ObjectHistory, IpInterval> loadTree() throws Exception;
}
