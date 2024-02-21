package data.scripts.net.data.plugins;

/**
 * General purpose one-way script for sending/receiving arbitrary data
 */
public abstract class ServerScript {

    static short SCRIPT_ID = -1;

    /**
     * Called on SERVER
     * @param amount delta time
     */
    abstract void advance(float amount);

    /**
     * Called on CLIENT
     * @param amount delta time
     */
    abstract void execute(float amount);

    static

    short getID() {
        return SCRIPT_ID;
    }

}
