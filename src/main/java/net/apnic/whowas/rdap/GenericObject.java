package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonValue;
import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericObject implements RdapObject, Serializable {
    private static final long serialVersionUID = 1732825639472752224L;

    private final ObjectKey objectKey;
    private final byte[] rpsl;
    private final Collection<RdapObject> relatedObjects;

    public GenericObject(ObjectKey objectKey, byte[] rpsl) {
        this(objectKey, rpsl, Collections.emptySet());
    }

    private GenericObject(ObjectKey objectKey, byte[] rpsl, Collection<RdapObject> relatedObjects) {
        this.objectKey = objectKey;
        this.rpsl = rpsl;
        this.relatedObjects = relatedObjects;
    }

    @Override
    public ObjectKey getObjectKey() {
        return objectKey;
    }

    private static final Pattern ENTITY_KEYS =
            Pattern.compile("^(?:admin-c|tech-c|zone-c|mnt-irt):\\s*(.*?)$",
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    @Override
    public Collection<ObjectKey> getEntityKeys() {
        if (objectKey.getObjectClass() == ObjectClass.ENTITY) {
            return Collections.emptySet();
        }

        Matcher m = ENTITY_KEYS.matcher(new String(rpsl, Charset.forName("UTF-8")));
        Set<ObjectKey> keys = new HashSet<>();
        while (m.find()) {
            keys.add(new ObjectKey(ObjectClass.ENTITY, m.group(1)));
        }
        return keys;
    }

    @Override
    public boolean isDeleted()
    {
        return rpsl.length == 0;
    }

    @Override
    public RdapObject withEntities(Collection<RdapObject> relatedEntities) {
        return new GenericObject(objectKey, rpsl, relatedEntities);
    }

    @JsonValue
    public Map<String, Object> toJSON() {
        switch (objectKey.getObjectClass()) {
            case IP_NETWORK:
                return IpNetwork.fromBytes(objectKey, rpsl, relatedObjects);
            case DOMAIN:
                return Domain.fromBytes(objectKey, rpsl, relatedObjects);
            case AUT_NUM:
                return AutNum.fromBytes(objectKey, rpsl, relatedObjects);
            case ENTITY:
                return Entity.fromBytes(objectKey, rpsl, relatedObjects);
        }

        return null;
    }
}
