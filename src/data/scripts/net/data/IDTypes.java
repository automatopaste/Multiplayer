package data.scripts.net.data;

/**
 * Stores static integer IDs. Should be replaced with a dynamic allocation system at some point and also use
 * better byte compression
 */
public class IDTypes {
    // entities
    public static final int SHIP = 1;
    public static final int INPUT_AGGREGATE = 2;
    public static final int SIMPLE_ENTITY = 3;

    // data record types
    public static final int FLOAT_RECORD = 101;
    public static final int INT_RECORD = 102;
    public static final int V2F_RECORD = 103;
    public static final int STRING_RECORD = 104;
}
