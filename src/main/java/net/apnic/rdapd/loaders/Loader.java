package net.apnic.rdapd.loaders;

import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.Revision;

/**
 * Load history data from some source.
 *
 * Data will be fed one revision at a time into a BiConsumer.
 */
public interface Loader {
    void loadWith(RevisionConsumer revisionConsumer);

    interface RevisionConsumer {
        void accept(ObjectKey objectKey, Revision revision);
    }
}
