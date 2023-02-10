package data.scripts.net.data.packables;

import data.scripts.net.data.InstanceData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to read/write data
 */
public abstract class EntityData {

    protected final short instanceID;
    protected final List<RecordLambda<?>> records;
    protected final List<InterpRecordLambda<?>> interpolate;
    private boolean init = true;

    public EntityData(short instanceID) {
        this.instanceID = instanceID;

        records = new ArrayList<>();
        interpolate = new ArrayList<>();
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

    protected void addInterpRecord(InterpRecordLambda<?> recordLambda) {
        addRecord(recordLambda);
        interpolate.add(recordLambda);
    }

    public List<RecordLambda<?>> getRecords() {
        return records;
    }

    /**
     * Returns a map of all records that are marked as updated since the last time
     * @return deltas
     */
    public InstanceData sourceExecute(float amount) {
        Map<Byte, DataRecord<?>> deltas = new HashMap<>();
        int size = 0;

        for (byte i = 0; i < records.size(); i++) {
            RecordLambda<?> recordLambda = records.get(i);

            if (recordLambda.sourceExecute(amount) || init) {
                deltas.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }
        }

        init = false;

        return new InstanceData(size, deltas);
    }

    /**
     * Update stored data with changes from a delta at dest
     * @param deltas incoming deltas
     */
    public void destExecute(Map<Byte, Object> deltas, int tick) {
        for (byte k : deltas.keySet()) {
            RecordLambda<?> record = records.get(k);
            record.overwrite(tick, deltas.get(k));
        }

        for (RecordLambda<?> recordLambda : records) recordLambda.destExecute(this);
    }

    public void interp(float delay) {
        for (InterpRecordLambda<?> interpRecordLambda : interpolate) {
            interpRecordLambda.interp(delay, this);
        }
    }

    /**
     * Called every time an entity plugin updates on the game thread. May be called by either client or server
     */
    public abstract void update(float amount, BaseEntityManager manager);

    /**
     * Called when entity is initialised
     */
    public abstract void init(MPPlugin plugin, InboundEntityManager manager);

    /**
     * Called when entity is deleted
     */
    public abstract void delete();
}
