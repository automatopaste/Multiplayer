package data.scripts.net.data;

import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.records.ARecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows mods to specify entity types and record types dynamically
 */
public class DataGenManager {
    public static Map<Class<? extends APackable>, Integer> entityTypeIDs = new HashMap<>();
    public static Map<Integer, APackable> entityInstances = new HashMap<>();

    public static Map<Class<? extends ARecord<?>>, Integer> recordTypeIDs = new HashMap<>();
    public static Map<Integer, ARecord<?>> recordInstances = new HashMap<>();

    private static int idIncrementer = 1;

    public synchronized static int registerEntityType(Class<? extends APackable> clazz, APackable instance) {
        int id = idIncrementer;
        entityTypeIDs.put(clazz, id);
        entityInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public synchronized static int registerRecordType(Class<? extends ARecord<?>> clazz, ARecord<?> instance) {
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
    public static APackable entityFactory(int typeID) {
        return entityInstances.get(typeID);
    }

    /**
     * Hacky workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static ARecord<?> recordFactory(int typeID) {
        return recordInstances.get(typeID);
    }
}
