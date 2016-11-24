package net.apnic.whowas.history;

import net.apnic.whowas.rdap.RdapObject;

import java.io.*;
import java.time.ZonedDateTime;

/**
 * A Revision is one version of one object in a registry's history.
 */
public class Revision implements Serializable {
    private static final long serialVersionUID = 263521376882066215L;

    private final int sequence;
    private final ZonedDateTime validFrom;
    private final ZonedDateTime validUntil;
    private final RdapObject contents;

    /**
     * Construct a new revision for an object.
     *
     * @param sequence   The sequence number this revision has
     * @param validFrom  When this revision came into effect
     * @param validUntil When this revision was superseded
     * @param contents   The object's revision, as RDAP data
     */
    public Revision(int sequence, ZonedDateTime validFrom, ZonedDateTime validUntil, RdapObject contents) {
        this.sequence   = sequence;
        this.validFrom  = validFrom;
        this.validUntil = validUntil;
        this.contents   = contents;
    }

    /**
     * Supersede this Revision at the given date.
     *
     * @param validUntil the date at which the object was superseded
     * @return A new Revision with an updated validity range
     */
    public Revision supersede(ZonedDateTime validUntil) {
        return new Revision(sequence, validUntil, validUntil, contents);
    }


    public RdapObject getContents() {
        return contents;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public ZonedDateTime getValidUntil() {
        return validUntil;
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(sequence, validFrom, validUntil, contents);
    }

    private static class Wrapper implements Serializable {
        private static final long serialVersionUID = 4063426002249141977L;

        private int sequence;
        private ZonedDateTime validFrom;
        private ZonedDateTime validUntil;
        private RdapObject contents;

        public Wrapper(int sequence, ZonedDateTime validFrom, ZonedDateTime validUntil, RdapObject contents) {
            this.sequence = sequence;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
            this.contents = contents;
        }

        private Object readResolve() {
            return new Revision(sequence, validFrom, validUntil, contents);
        }
    }
}
