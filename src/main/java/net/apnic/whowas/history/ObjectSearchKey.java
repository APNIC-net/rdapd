package net.apnic.whowas.history;

public class ObjectSearchKey
    extends ObjectKey
{
    private String attribute;

    public ObjectSearchKey(ObjectClass objectClass, String attribute,
                           String objectName)
    {
        super(objectClass, objectName);
        this.attribute = attribute;
    }

    public String getAttribute()
    {
        return attribute;
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
