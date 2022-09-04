package data.scripts.net.data;

import data.scripts.plugins.MPPlugin;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Used to store data on remote/host and used to read/write data
 */
public abstract class BasePackable {
    protected final ByteBuffer packer;

    private final int instanceID;

    protected boolean initFlush = true;

    public BasePackable(int instanceID) {
        this.instanceID = instanceID;

        packer = ByteBuffer.allocate(1024);
    }

    public byte[] pack(boolean flush) {
        packer.clear();

        // so packer type can be identified
        packer.putInt(getTypeId());
        // so packer instance can be identified
        packer.putInt(getInstanceID());

        if (flush || initFlush) {
            write(true);
        } else {
            if (!write(false)) return null;
        }

        packer.flip();
        byte[] out = new byte[packer.remaining()];
        packer.get(out);

        initFlush = false;

        return out;
    }

    public int getInstanceID() {
        return instanceID;
    }

    /**
     * Called every time an entity plugin updates on the game thread. May be called by either client or server
     */
    public abstract void destinationUpdate();

    /**
     * Called when entity is initialised at destination
     */
    public abstract void destinationInit(MPPlugin plugin);

    /**
     * Called when entity is deleted on dest
     */
    public abstract void destinationDelete();

    /**
     * Useful when a packable is used for a consumable single-use task
     * @return if it should be deleted from the destination outbound entity manager
     */
    public abstract boolean isTransient();

    /**
     * Update stored data with changes from a delta
     * @param delta incoming delta (still uses same class as non-transmitting entity for convenience)
     */
    public abstract void updateFromDelta(BasePackable delta);

    /**
     * Ouput data to a byte buffer
     * @return true if data has update, false if no updated data and entity can be ignored by packer
     */
    protected abstract boolean write(boolean flush);

    /**
     * Immutable ID type to identify and construct entity when decoding packet
     * @return id
     */
    public abstract int getTypeId();

    /**
     * Unpack data into a new packet instance
     * @param instanceID instance of ID to identify between client/server
     * @param records maps unique IDs to record
     * @return new instance of packable with data
     */
    public abstract BasePackable unpack(int instanceID, Map<Integer, BaseRecord<?>> records);
}
