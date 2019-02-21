package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.types.IP;
import net.apnic.rdapd.types.IpInterval;

import java.util.function.BiFunction;

/**
 * IpNetwork RDAP object.
 */
public class IpNetwork extends GenericObject {
    private final IpInterval ipInterval;
    private String type;

    /**
     * Constructs a new network object with the given key and interval.
     *
     * @param objectKey Key for this object
     * @param ipInterval Ip range this network object represents
     */
    public IpNetwork(ObjectKey objectKey, IpInterval ipInterval)
    {
        super(objectKey);
        this.ipInterval = ipInterval;
    }

    /**
     * Provides the end address parameter used in constructing JSON response.
     *
     * @return String representation of end address in this network object
     */
    public String getEndAddress()
    {
        return ipInterval.high().toString();
    }

    /**
     * Provides the Ip netowkr interval this object represents.
     *
     * @return IpInterval of the network
     */
    @JsonIgnore
    public IpInterval getIpInterval()
    {
        return ipInterval;
    }

    /**
     * Provides the ip version parameter used in constructing JSON response.
     *
     * @return String representation of the ip version of this network object
     */
    public String getIpVersion()
    {
        return ipInterval.low().getAddressFamily() == IP.AddressFamily.IPv4 ?
            "v4" : "v6";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.IP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathHandle()
    {
        return getIpInterval().toCIDRString();
    }

    /**
     * Provides the start address parameter used in constructing JSON response.
     *
     * @return String representation of start address in this network object
     */
    public String getStartAddress()
    {
        return ipInterval.low().toString();
    }

    /**
     * Provides the type parameter used in constructing JSON response.
     *
     * @return String representation of type for this network object
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of this network object.
     *
     * @param type Network type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Provides the CIDR representation for the resource block according to NRO's
     * <a href="https://gitlab.nro.net/ecg/draft-rdap-cidr">CIDR Expressions in RDAP</a>.
     */
    @JsonProperty("cidr0_cidrs")
    public Cidr0Object[] getCidr0Cidrs() {
        BiFunction<String, String, Cidr0Object> cons =
                ipInterval.low().getAddressFamily() == IP.AddressFamily.IPv4
                        ? Cidr0Ipv4::new
                        : Cidr0Ipv6::new;
        return new Cidr0Object[] {cons.apply(ipInterval.low().toString(), Integer.toString(ipInterval.prefixSize()))};
    }

    public String toString()
    {
        return String.format("ip: %s", ipInterval);
    }
}
