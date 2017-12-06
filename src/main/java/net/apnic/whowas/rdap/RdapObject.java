package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.apnic.whowas.history.ObjectKey;

import java.util.Collection;
import java.util.Collections;

/**
 * Abstraction of an RDAP object
 */
public interface RdapObject
{
    /**
     * The keys of the entities this object references.
     *
     * @return the keys of the entities this object references.
     **/
    @JsonIgnore
    default Collection<ObjectKey> getEntityKeys() {
        return Collections.emptySet();
    }

    /**
     * The key of this RDAP object
     *
     * @return the key of this RDAP object
     */
    @JsonIgnore
    ObjectKey getObjectKey();

    @JsonIgnore
    default Collection<RelatedEntity> getRelatedEntities()
    {
        return Collections.emptyList();
    }

    /**
     * Whether this object has been considered deleted out of current state.
     */
    @JsonIgnore
    default boolean isDeleted()
    {
        return false;
    }

    /**
     * Create a new RdapObject with the given related entities incorporated.
     *
     * @param relatedEntities the entities to incorporate.
     * @return an RdapObject with the given related entities incorporated.
     */
    default RdapObject withRelatedEntities(
        Collection<RelatedEntity> relatedEntities)
    {
        return this;
    }
}
