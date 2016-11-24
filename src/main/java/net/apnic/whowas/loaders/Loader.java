package net.apnic.whowas.loaders;

import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.Revision;

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
