package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.apnic.whowas.history.ObjectKey;

/**
 * Entity RDAP object.
 */
public class Entity
    extends GenericObject
{
    private VCard vCard = null;

    /**
     * Constructs a new Entity object with the given key.
     *
     * @param objectKey key for this object
     */
    public Entity(ObjectKey objectKey)
    {
        super(objectKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Collection<RdapObject> getEntities()
    {
        return super.getEntities();
    }

    /**
     * {@inheritDoc}
     */
    public String getObjectClassName()
    {
        return "entity";
    }

    /**
     * Returns the VCard set on this entity object.
     *
     * @return The set VCard
     */
    @JsonIgnore
    public VCard getVCard()
    {
        return this.vCard;
    }

    /**
     * Returns the VCard property needed for JSON responses.
     *
     * This function does not have much use in code except as a formatting
     * method for responses.
     *
     * @return VCardArray object
     */
    public List<Object> getVCardArray()
    {
        return Arrays.asList(
            "vcard", getVCard());
    }

    /**
     * Sets this entity objects vcard.
     *
     * @param vCard The vcard to set on this entity
     */
    public void setVCard(VCard vCard)
    {
        this.vCard = vCard;
    }
}
