package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.apnic.whowas.history.ObjectKey;

/**
 * Generic abstract parent class for concrete RDAP objects.
 */
public abstract class GenericObject
    implements Cloneable, Serializable, RdapObject
{
    private static final long serialVersionUID = 2017_09_26L;

    private String country = null;
    private boolean deleted = false;
    private List<Event> events = Collections.emptyList();
    private final ObjectKey objectKey;
    private String name = null;
    private Collection<RelatedEntity> relatedEntities;
    private ArrayNode remarks = null;

    /**
     * Creates a new GenericObject with the supplied key.
     *
     * @param objectKey
     */
    public GenericObject(ObjectKey objectKey)
    {
        this(objectKey, Collections.emptyList());
    }

    /**
     * Creates a new GenericObject with the supplied key and related objects
     * index.
     *
     * @param objectKey
     * @param relatedObjects
     */
    public GenericObject(ObjectKey objectKey,
                         Collection<RelatedEntity> relatedEntities)
    {
        this.objectKey = objectKey;
        this.relatedEntities = relatedEntities;
    }

    /**
     * Returns the country set for this object.
     *
     * Generic method for defining common attributes to include in JSON rdap
     * responses.
     *
     * @return Country set on this object
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCountry()
    {
        return this.country;
    }

    @JsonIgnore
    @Override
    public Collection<ObjectKey> getEntityKeys()
    {
        return getRelatedEntities().stream()
            .map(RelatedEntity::getObjectKey)
            .collect(Collectors.toList());
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Event> getEvents()
    {
        return events;
    }

    /**
     * Returns this objects handle.
     *
     * @return Objects handle
     */
    public String getHandle()
    {
        return getObjectKey().getObjectName();
    }

    /**
     * Returns the name set for this object.
     *
     * @return The set name
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getName()
    {
        return this.name;
    }

    /**
     * Provides the object class name for children of this class.
     *
     * Must be implemented by children.
     *
     * @return object class name
     */
    public abstract String getObjectClassName();

    /**
     * Returns the object key this object was constructed with.
     *
     * @return ObjectKey this object was constructed with
     */
    @Override
    @JsonIgnore
    public ObjectKey getObjectKey()
    {
        return objectKey;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("entities")
    @Override
    public Collection<RelatedEntity> getRelatedEntities()
    {
        return relatedEntities;
    }

    /**
     * Return the remarks set on this object.
     *
     * Data is stored as ready to go JSON.
     *
     * @return Set remarks
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public ArrayNode getRemarks()
    {
        if(remarks == null)
        {
            return new ArrayNode(JsonNodeFactory.instance);
        }
        return remarks;
    }

    /**
     * {@inheritDocs}
     */
    @Override
    @JsonIgnore
    public boolean isDeleted()
    {
        return deleted;
    }

    /**
     * Sets the country this object represents.
     *
     * @param country Country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * Sets the deleted status.
     *
     * @param deleted Whether this object is considered deleted or not.
     */
    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public void setEvents(List<Event> events)
    {
        this.events = events;
    }

    /**
     * Sets the name for this object.
     *
     * @param name Name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void setRelatedEntities(Collection<RelatedEntity> relatedEntities)
    {
        this.relatedEntities = relatedEntities;
    }

    /**
     * Sets remarks associated with object.
     *
     * Value is supplied as JSON for casting into the final RDAP output.
     *
     * @param remarks Remarks to set
     */
    public void setRemarks(ArrayNode remarks)
    {
        this.remarks = remarks;
    }

    /**
     * {@inheritDocs}
     */
    @Override
    public RdapObject withRelatedEntities(Collection<RelatedEntity> relatedEntities)
    {
        try
        {
            GenericObject go = (GenericObject)clone();
            go.relatedEntities = relatedEntities;
            return go;
        }
        catch(CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
