package net.apnic.whowas.loaders;

public class RipeDbLoaderUtil
{
    private RipeDbLoaderUtil() {}

    public static final String LOAD_QUERY_WITHOUT_SERIAL =
        "(SELECT object_id, object_type, pkey, sequence_id,\n" +
                "timestamp, object\n" +
        "FROM last\n" +
        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18))\n" +

        "UNION\n" +
        "(SELECT object_id, object_type, pkey, sequence_id,\n" +
         "timestamp, object\n" +
        "FROM history\n" +
        "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18))\n" +
        "ORDER BY timestamp, object_id, sequence_id";

    public static final String LOAD_QUERY_WITH_SERIAL =
        "(SELECT l.object_id, l.object_type, l.pkey, l.sequence_id,\n" +
                "l.timestamp, l.object\n" +
         "FROM last l, serials s\n" +
         "WHERE l.object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
         "AND l.object_id = s.object_id\n" +
         "AND l.sequence_id = s.sequence_id\n" +
         "AND s.serial_id > ?)\n" +

        "UNION\n" +
        "(SELECT h.object_id, h.object_type, h.pkey, h.sequence_id,\n" +
                "h.timestamp, h.object\n" +
         "FROM history h, serials s\n" +
         "WHERE object_type in (0, 2, 3, 5, 6, 9, 10, 11, 17, 18)\n" +
         "AND h.object_id = s.object_id\n" +
         "AND h.sequence_id = s.sequence_id\n" +
         "AND s.serial_id > ?)\n" +
         "ORDER BY timestamp, object_id, sequence_id";

    public static final String SERIAL_MAX =
        "SELECT MAX(serial_id) AS 'serial' FROM serials";
}
