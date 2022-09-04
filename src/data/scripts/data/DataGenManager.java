package data.scripts.data;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;
import org.lazywizard.console.Console;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows mods to specify entity types and record types at runtime
 */
public class DataGenManager {
    public static Map<Class<? extends BasePackable>, Integer> entityTypeIDs = new HashMap<>();
    public static Map<Integer, BasePackable> entityInstances = new HashMap<>();

    public static Map<Class<? extends BaseRecord<?>>, Integer> recordTypeIDs = new HashMap<>();
    public static Map<Integer, BaseRecord<?>> recordInstances = new HashMap<>();

    public static Map<Integer, InboundEntityManager> inboundDataDestinations = new HashMap<>();

    private static int idIncrementer = 1;

    public static int registerEntityType(Class<? extends BasePackable> clazz, BasePackable instance) {
        int id = idIncrementer;
        entityTypeIDs.put(clazz, id);
        entityInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public static int registerRecordType(Class<? extends BaseRecord<?>> clazz, BaseRecord<?> instance) {
        int id = idIncrementer;
        recordTypeIDs.put(clazz, id);
        recordInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public static void registerEntityManager(int dataTypeID, InboundEntityManager manager) {
        inboundDataDestinations.put(dataTypeID, manager);
    }

    public static void distributeInboundDeltas(Map<Integer, BasePackable> inbound, MPPlugin plugin) {
        for (Integer id : inbound.keySet()) {
            BasePackable delta = inbound.get(id);

            if (delta.isTransient()) {
                delta.destinationInit(plugin);
                continue;
            }

            inboundDataDestinations.get(delta.getTypeId()).processDelta(id, delta, plugin);
        }
    }

    /**
     * Hacky workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static BasePackable entityFactory(int typeID) {
        BasePackable out = entityInstances.get(typeID);
        if (out == null) Console.showMessage("No entity packable type found at ID: " + typeID);
        return out;
    }

    /**
     * Hacky workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static BaseRecord<?> recordFactory(int typeID) {
        BaseRecord<?> out = recordInstances.get(typeID);
        if (out == null) Console.showMessage("No record type found at ID: " + typeID);
        return out;
    }
}
