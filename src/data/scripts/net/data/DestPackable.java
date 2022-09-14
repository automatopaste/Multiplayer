package data.scripts.net.data;

import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class DestPackable extends BasePackable {

    /**
     * Destination constructor
     * @param instanceID unique
     * @param records incoming deltas
     */
    public DestPackable(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID);

        this.records = new HashMap<>();

        initDefaultRecords();

        for (Integer key : records.keySet()) {
            BaseRecord<?> record = records.get(key);
            putRecord(record);
        }
    }

    protected abstract void initDefaultRecords();

    /**
     * Called every time an entity plugin updates on the game thread. May be called by either client or server
     */
    public abstract void update(float amount);

    /**
     * Called when entity is initialised at destination
     */
    public abstract void init(MPPlugin plugin);

    /**
     * Called when entity is deleted at destination
     */
    public abstract void delete();
}
