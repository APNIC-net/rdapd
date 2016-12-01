package net.apnic.whowas.history;

import java.io.*;
import java.util.Objects;

/**
 *  The key identifying one unique object in the registry's history.
 *
 *  Identity of objects should match what would have been the result of an
 *  exact match query at some point in time.  That is, if some object would
 *  have been the exact match result of searching for "EXAMPLE" last week,
 *  and some object would be the exact match today, then those two objects
 *  have the same identity, if different revisions.
 *
 *  This does not necessarily match the internal notion of identity of a
 *  registry service, but _does_ match user expectations of asking what
 *  revisions are contained in the history of the object labelled "EXAMPLE".
 */
public class ObjectKey implements Serializable {
    private static final long serialVersionUID = 455037070872681136L;

    private final ObjectClass objectClass;
    private final String objectName;

    public ObjectKey(ObjectClass objectClass, String objectName) {
        this.objectClass = objectClass;
        this.objectName = objectName;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof ObjectKey
                && objectClass == ((ObjectKey) o).objectClass
                && Objects.equals(objectName, ((ObjectKey) o).objectName);
    }

    @Override
    public int hashCode() {
        return 41 * (41 + objectClass.hashCode()) + objectName.hashCode();
    }

    @Override
    public String toString() {
        return "{\"" + objectClass.toString() + "\": \"" + objectName + "\"}";
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(objectClass, objectName);
    }

    private static class Wrapper implements Serializable {
        private ObjectClass objectClass;
        private String objectName;

        public Wrapper(ObjectClass objectClass, String objectName) {
            this.objectClass = objectClass;
            this.objectName = objectName;
        }

        private Object readResolve() {
            return new ObjectKey(objectClass, objectName);
        }
    }
}
