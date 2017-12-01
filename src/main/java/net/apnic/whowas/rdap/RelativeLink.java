package net.apnic.whowas.rdap;

/**
 * Convience class that describes relative links which need to be translated to
 * absoulte paths on serialisation.
 */
public class RelativeLink
    extends Link
{
    public RelativeLink(String rel, String href, String type)
    {
        super(rel, href, type);
    }
}
