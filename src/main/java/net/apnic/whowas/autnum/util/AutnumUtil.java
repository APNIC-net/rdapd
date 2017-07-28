package net.apnic.whowas.autnum.util;

/**
 * Utility class for dealing with autnum's.
 */
public class AutnumUtil
{
    public static final long AUTNUM_MAX_SIZE = 0xFFFFFFFFL;
    public static final long AUTNUM_MIN_SIZE = 0x0L;

    /**
     * Checks a given string to see if it is a valid autnum.
     *
     * A valid autnum is one that is not < AUTNUM_MIN_SIZE and not >
     * AUTNUM_MAX_SIZE.
     *
     * @param strAutnum String to test for a valid autnum
     * @return True if strAutnum is a valid autnum
     */
    public static boolean isValidAutnum(String strAutnum)
    {
        try
        {
            long autnum = Long.parseLong(strAutnum);
            return autnum >= AUTNUM_MIN_SIZE && autnum <= AUTNUM_MAX_SIZE;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
}
