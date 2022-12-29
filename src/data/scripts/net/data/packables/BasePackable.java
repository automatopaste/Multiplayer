package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to read/write data
 */
public abstract class BasePackable {

    protected final short instanceID;
    protected final List<RecordLambda<?>> records;
    protected final Map<Byte, BaseRecord<?>> deltas;
    protected boolean initialForce = true;

    public BasePackable(short instanceID) {
        this.instanceID = instanceID;

        records = new ArrayList<>();
        deltas = new HashMap<>();
    }

    public short getInstanceID() {
        return instanceID;
    }

    /**
     * Immutable ID type to identify and construct entity when decoding packet
     * @return id
     */
    public abstract byte getTypeID();

    protected void addRecord(RecordLambda<?> record) {
        records.add(record);
    }

    public List<RecordLambda<?>> getRecords() {
        return records;
    }

    /**
     * Returns a map of all records that are marked as updated since the last time
     * @return deltas
     */
    public Map<Byte, BaseRecord<?>> getDeltas() {
        return deltas;
    }

    public void execute() {
        for (RecordLambda<?> recordLambda : records) recordLambda.execute(this);
    }

    /**
     * Update stored data with changes from a delta
     * @param deltas incoming deltas
     */
    public void overwrite(Map<Byte, BaseRecord<?>> deltas) {
        this.deltas.clear();

        for (byte k : deltas.keySet()) {
            RecordLambda<?> record = records.get(k);
            record.overwrite(deltas.get(k));

            if (record.record.isUpdated()) this.deltas.put(k, record.record);
        }
    }

    /**
     * Ouput data to a byte buffer
     */
//    public void write(boolean force, ByteBuf dest) {
//        ByteBuf temp = PooledByteBufAllocator.DEFAULT.buffer();
//
//        boolean f = force || initialForce;
//
//        for (byte i = 0; i < records.size(); i++) {
//            records.get(i).record.write(f, temp, i);
//        }
//
//        if (temp.readableBytes() > 0) {
//            // so packer type can be identified
//            dest.writeByte(getTypeID());
//            // so packer instance can be identified
//            dest.writeShort(getInstanceID());
//
//            dest.writeBytes(temp);
//        }
//
//        temp.release();
//        initialForce = false;
//    }

    /**
     * Called every time an entity plugin updates on the game thread. May be called by either client or server
     */
    public abstract void update(float amount);

    /**
     * Called when entity is initialised
     */
    public abstract void init(MPPlugin plugin);

    /**
     * Called when entity is deleted
     */
    public abstract void delete();
}
