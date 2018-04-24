package net.apnic.rdapd.history;

import net.apnic.rdapd.rdap.RdapObject;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A Revision is one version of one object in a registry's history.
 */
public class Revision implements Serializable {
    private static final long serialVersionUID = -8401990997863142475L;

    private final ZonedDateTime validFrom;
    private final ZonedDateTime validUntil;
    private final RdapObject contents;

    /**
     * Construct a new revision for an object.
     *
     * @param validFrom  When this revision came into effect
     * @param validUntil When this revision was superseded
     * @param contents   The object's revision, as RDAP data
     */
    public Revision(ZonedDateTime validFrom, ZonedDateTime validUntil,
                    RdapObject contents) {
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.contents = contents;
    }

    /**
     * Supersede this Revision at the given date.
     *
     * @param validUntil the date at which the object was superseded
     * @return A new Revision with an updated validity range
     */
    public Revision supersede(ZonedDateTime validUntil) {
        return new Revision(validFrom, validUntil, contents);
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
        return new Wrapper(validFrom, validUntil, contents);
    }

    private static class Wrapper implements Serializable {
        private static final long serialVersionUID = 4063426002249141977L;

        private ZonedDateTime validFrom;
        private ZonedDateTime validUntil;
        private RdapObject contents;

        private Wrapper(ZonedDateTime validFrom, ZonedDateTime validUntil, RdapObject contents) {
            this.validFrom = validFrom;
            this.validUntil = validUntil;
            this.contents = contents;
        }

        private Object readResolve() {
            return new Revision(validFrom, validUntil, contents);
        }
    }
}
