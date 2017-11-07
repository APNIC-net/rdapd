package net.apnic.whowas.history;

public class ObjectSearchKey {
    private final String attribute;
    private final ObjectClass objectClass;
    private final String objectName;
    private final ObjectSearchType objectSearchType;

    public ObjectSearchKey(ObjectClass objectClass, String attribute,
                           String objectName,
                           ObjectSearchType objectSearchType) {
        this.attribute = attribute;
        this.objectClass = objectClass;
        this.objectName = objectName;
        this.objectSearchType = objectSearchType;
    }

    public ObjectSearchKey(ObjectClass objectClass, String attribute,
                           String objectName) {
        this(objectClass, attribute, objectName, ObjectSearchType.STANDARD);
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

    public ObjectSearchType getObjectSearchType() {
        return objectSearchType;
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
