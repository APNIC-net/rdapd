package net.apnic.whowas.loaders;

import net.apnic.whowas.history.History;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;

public interface Loader {
    IntervalTree<IP, History, IpInterval> loadTree() throws Exception;
}
