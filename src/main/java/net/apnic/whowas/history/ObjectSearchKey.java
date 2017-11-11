package net.apnic.whowas.history;

import java.text.Normalizer;

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
        this.objectSearchType = objectSearchType;

        if (objectSearchType == ObjectSearchType.REGEX) {
            objectName =
                (objectName.matches("\\^.*"))
                    ? objectName.substring(1)
                    : ".*" + objectName;
            objectName =
                (objectName.matches(".*\\$"))
                    ? objectName.substring(0, objectName.length() - 1)
                    : objectName + ".*";
            objectName = objectName.toLowerCase();
        }

        this.objectName =
            Normalizer.normalize(objectName,
                                 Normalizer.Form.NFKC);
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
