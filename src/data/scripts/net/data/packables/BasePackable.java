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
    private boolean init = true;

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
//        Map<Byte, BaseRecord<?>> deltas = new HashMap<>();
//        for (byte i = 0; i < records.size(); i++) {
//            RecordLambda<?> lambda = records.get(i);
//            deltas.put(i, lambda.record);
//        }
        if (init) {
            for (byte i = 0; i < records.size(); i++) {
                RecordLambda<?> lambda = records.get(i);
                deltas.put(i, lambda.record);
            }
        }

        return deltas;
    }

    public void sourceExecute() {
        deltas.clear();

        for (byte i = 0; i < records.size(); i++) {
            RecordLambda<?> recordLambda = records.get(i);
            if (recordLambda.sourceExecute()) this.deltas.put(i, recordLambda.record);
        }
    }

    public void destExecute() {
        for (RecordLambda<?> recordLambda : records) recordLambda.destExecute(this);
    }

    /**
     * Update stored data with changes from a delta at dest
     * @param deltas incoming deltas
     */
    public void overwrite(Map<Byte, BaseRecord<?>> deltas) {
        for (byte k : deltas.keySet()) {
            RecordLambda<?> record = records.get(k);
            record.overwrite(deltas.get(k));
        }
    }

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

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }
}
