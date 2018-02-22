package net.apnic.rdapd.history;

public class ObjectSearchKey {
    private final String attribute;
    private final ObjectClass objectClass;
    private final String objectName;

    public ObjectSearchKey(ObjectClass objectClass, String attribute,
                           String objectName) {
        this.attribute = attribute;
        this.objectClass = objectClass;
        this.objectName = objectName;
    }

    public String getAttribute()
    {
        return attribute;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public boolean equals(Object o)
    {
        return super.equals(o) && o instanceof ObjectSearchKey
            && attribute.equals(((ObjectSearchKey)o).attribute);
    }

    @Override
    public int hashCode()
    {
        return 2;
    }
}
