package data.scripts.net.data;

/**
 * Stores static IDs. Should be replaced with a dynamic allocation system at some point
 */
public class IDTypes {
    // entities
    public static final int SHIP = 1;

    // data record types
    public static final int FLOAT_RECORD = 100;
    public static final int V2F_RECORD = 101;

    // record significance types
    public static final int SHIP_POS = 201;
    public static final int SHIP_VEL = 202;
    public static final int SHIP_ANG = 203;
    public static final int SHIP_ANGVEL = 204;
    public static final int SHIP_HULL = 205;
    public static final int SHIP_FLUX = 206;
}
