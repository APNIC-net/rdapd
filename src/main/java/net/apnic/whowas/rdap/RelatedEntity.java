package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import net.apnic.whowas.history.ObjectKey;

public class RelatedEntity
{
    private final ObjectKey key;
    private final Optional<RdapObject> object;
    private final Set<Role> roles;

    public RelatedEntity(ObjectKey key)
    {
        this(key, Collections.emptySet());
    }

    public RelatedEntity(ObjectKey key, Set<Role> roles)
    {
        this(key, roles, Optional.empty());
    }

    public RelatedEntity(ObjectKey key, Set<Role> roles, RdapObject object)
    {
        this(key, roles, Optional.ofNullable(object));
    }

    public RelatedEntity(ObjectKey key, Set<Role> roles,
                         Optional<RdapObject> object)
    {
        this.key = key;
        this.roles = roles;
        this.object = object;
    }

    @JsonUnwrapped
    public RdapObject getObjectAsNullable()
    {
        return object.orElse(null);
    }

    @JsonIgnore
    public Optional<RdapObject> getObject()
    {
        return object;
    }

    @JsonIgnore
    public ObjectKey getObjectKey()
    {
        return key;
    }

    public Set<Role> getRoles()
    {
        return roles;
    }

    public RelatedEntity withObject(Optional<RdapObject> object)
    {
        return new RelatedEntity(getObjectKey(), getRoles(), object);
    }
}
