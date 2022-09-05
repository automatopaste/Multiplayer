package data.scripts.net.data.util;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
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
    public static Map<Integer, OutboundEntityManager> outboundDataSources = new HashMap<>();

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

    public static void registerInboundEntityManager(int dataTypeID, InboundEntityManager manager) {
        inboundDataDestinations.put(dataTypeID, manager);
    }

    public static void registerOutboundEntityManager(int dataTypeID, OutboundEntityManager manager) {
        outboundDataSources.put(dataTypeID, manager);
    }

    public static void distributeInboundDeltas(Map<Integer, Map<Integer, BasePackable>> inbound, MPPlugin plugin) {
        for (Integer type : inbound.keySet()) {
            Map<Integer, BasePackable> entities = inbound.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            for (Integer instance : entities.keySet()) {
                manager.processDelta(instance, entities.get(instance), plugin);
            }
        }
    }

    public static Map<Integer, Map<Integer, BasePackable>> collectOutboundDeltas() {
        Map<Integer, Map<Integer, BasePackable>> out = new HashMap<>();

        for (Integer source : outboundDataSources.keySet()) {
            Map<Integer, BasePackable> entities = outboundDataSources.get(source).getOutbound();
            out.put(source, entities);
        }

        return out;
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
