package net.apnic.whowas.rdap;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role
{
    ABUSE("abuse"),
    ADMINISTRATIVE("administrative"),
    BILLING("billing"),
    NOC("noc"),
    NOTIFICATIONS("notifications"),
    PROXY("proxy"),
    REGISTRANT("registrant"),
    REGISTRAR("registrar"),
    RESELLER("reseller"),
    SPONSOR("sponsor"),
    TECHNICAL("technical");

    private final String value;
    Role(String value)
    {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString()
    {
        return value;
    }
}
