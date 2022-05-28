package data.scripts.data;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows mods to specify entity types and record types dynamically
 */
public class DataGenManager {
    public static Map<Class<? extends BasePackable>, Integer> entityTypeIDs = new HashMap<>();
    public static Map<Integer, BasePackable> entityInstances = new HashMap<>();

    public static Map<Class<? extends BaseRecord<?>>, Integer> recordTypeIDs = new HashMap<>();
    public static Map<Integer, BaseRecord<?>> recordInstances = new HashMap<>();

    private static int idIncrementer = 1;

    public synchronized static int registerEntityType(Class<? extends BasePackable> clazz, BasePackable instance) {
        int id = idIncrementer;
        entityTypeIDs.put(clazz, id);
        entityInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public synchronized static int registerRecordType(Class<? extends BaseRecord<?>> clazz, BaseRecord<?> instance) {
        int id = idIncrementer;
        recordTypeIDs.put(clazz, id);
        recordInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    /**
     * Hacky workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static BasePackable entityFactory(int typeID) {
        BasePackable out = entityInstances.get(typeID);
        if (out == null) throw new NullPointerException("No entity packable type found at ID: " + typeID);
        return out;
    }

    /**
     * Hacky workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static BaseRecord<?> recordFactory(int typeID) {
        BaseRecord<?> out = recordInstances.get(typeID);
        if (out == null) throw new NullPointerException("No record type found at ID: " + typeID);
        return out;
    }
}
