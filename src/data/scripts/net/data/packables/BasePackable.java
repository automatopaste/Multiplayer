package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to read/write data
 */
public abstract class BasePackable {

    protected final int instanceID;
    protected final Map<Integer, BaseRecord<?>> records;
    protected boolean initialForce = true;

    public BasePackable(int instanceID) {
        this.instanceID = instanceID;
        records = new HashMap<>();
    }

    public int getInstanceID() {
        return instanceID;
    }

    /**
     * Immutable ID type to identify and construct entity when decoding packet
     * @return id
     */
    public abstract int getTypeID();

    protected void putRecord(BaseRecord<?> record) {
        records.put(record.uniqueID, record);
    }

    public BaseRecord<?> getRecord(int uniqueID) {
        return records.get(uniqueID);
    }
    public Map<Integer, BaseRecord<?>> getRecords() {
        return records;
    }

    /**
     * Update stored data with changes from a delta
     * @param deltas incoming deltas
     */
    public void updateFromDelta(Map<Integer, BaseRecord<?>> deltas) {
        for (int k : deltas.keySet()) {
            records.get(k).updateFromDelta(deltas.get(k));
        }
    }

    /**
     * Ouput data to a byte buffer
     */
    public void write(boolean force, ByteBuf dest) {
        ByteBuf temp = PooledByteBufAllocator.DEFAULT.buffer();

        boolean f = force || initialForce;

        for (BaseRecord<?> record : records.values()) {
            record.write(f, temp);
        }

        if (temp.readableBytes() > 0) {
            // so packer type can be identified
            dest.writeByte(getTypeID());
            // so packer instance can be identified
            dest.writeShort(getInstanceID());

            dest.writeBytes(temp);
        }

        temp.release();
        initialForce = false;
    }
}
