package net.apnic.whowas.rdap;

import be.dnsbelgium.rdap.core.IPNetwork;
import be.dnsbelgium.rdap.core.Notice;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.rpsl.RpslObject;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * An IP network object.
 */
public class IpNetwork implements RdapObject, Serializable {
    private static final long serialVersionUID = -5015613771361131756L;

    @JsonUnwrapped
    private final transient IPNetwork ipNetwork;

    public IpNetwork(ObjectKey objectKey, byte[] rpsl) {
        assert objectKey.getObjectClass() == ObjectClass.IP_NETWORK;

        RpslObject rpslObject = new RpslObject(rpsl);

        // TODO
        // - links: self
        // - links: "head"/"current" if available
        // - remarks: from object remarks
        // - country: from object country
        // - type:    from object status
        // - name:    from object netname
        // - notices: any history notices required (?)
        // - events:  can I use this for applicability?
        // how to get related objects?

        IpInterval ipInterval = Parsing.parseInterval(objectKey.getObjectName());
        ipNetwork = new IPNetwork(
                /* links */     null,
                /* notices */   null,
                /* remarks */   rpslObject.getRemarks(),
                /* lang */      null,
                IPNetwork.OBJECT_CLASS_NAME,
                /* events */    null,
                /* status */    null,
                /* port43 */    null,
                /* handle */    objectKey.getObjectName(),
                /* start */     ipInterval.low().getAddress(),
                /* end */       ipInterval.high().getAddress(),
                /* name */      "NYI",
                /* type */      "NYI",
                /* country */   "NYI",
                /* parent */    null,
                /* entities */  null
        );
    }

    private IpNetwork(IPNetwork ipNetwork) {
        this.ipNetwork = ipNetwork;
    }

    private static final List<Notice> DELETED_REMARKS = Collections.singletonList(new Notice(
            "deleted",
            null,
            Collections.singletonList("This object has been deleted"),
            null
    ));


    public static IpNetwork deletedObject(ObjectKey objectKey) {
        IpInterval ipInterval = Parsing.parseInterval(objectKey.getObjectName());
        return new IpNetwork(new IPNetwork(
                null, null, DELETED_REMARKS, null, IPNetwork.OBJECT_CLASS_NAME,
                null, null, null, objectKey.getObjectName(), ipInterval.low().getAddress(),
                ipInterval.high().getAddress(), null, null, null, null, null));
    }

    /**** Serialization code below ****/

    /* Serialization via a replacement wrapper to preserve immutability */
    private Object writeReplace() throws ObjectStreamException {
        return new Wrapper(RdapSerializing.serialize(ipNetwork));
    }

    private static final class Wrapper implements Serializable {
        private static final long serialVersionUID = -8825651861679233094L;
        private final byte[] data;

        private Wrapper(byte[] data) {
            this.data = data;
        }

        private Object readResolve() {
            return new IpNetwork(RdapSerializing.deserialize(data, IPNetwork.class));
        }
    }

}
