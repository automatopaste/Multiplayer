package data.scripts.net.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store data on remote/host and used to read/write data
 */
public abstract class BasePackable {

    protected final int instanceID;
    protected Map<Integer, BaseRecord<?>> records;

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
    public abstract int getTypeId();

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
}
