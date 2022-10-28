package data.scripts.net.data.util;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows mods to specify entity types and record types at runtime
 */
public class DataGenManager {
    public static Map<Class<? extends BasePackable>, Byte> entityTypeIDs = new HashMap<>();
    public static Map<Byte, DestPackable> entityInstances = new HashMap<>();

    public static Map<String, Byte> recordTypeIDs = new HashMap<>();
    public static Map<Byte, BaseRecord<?>> recordInstances = new HashMap<>();

    public static Map<Integer, InboundEntityManager> inboundDataDestinations = new HashMap<>();
    public static Map<Integer, OutboundEntityManager> outboundDataSources = new HashMap<>();

    private static int idIncrementer = 1;

    public static int registerEntityType(Class<? extends BasePackable> clazz, DestPackable instance) {
        byte id = (byte) idIncrementer;
        entityTypeIDs.put(clazz, id);
        entityInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public static int registerRecordType(String c, BaseRecord<?> instance) {
        int id = idIncrementer;
        recordTypeIDs.put(c, (byte) id);
        recordInstances.put((byte) id, instance);
        idIncrementer++;
        return id;
    }

    public static void registerInboundEntityManager(int dataTypeID, InboundEntityManager manager) {
        inboundDataDestinations.put(dataTypeID, manager);
    }

    public static void registerOutboundEntityManager(int dataTypeID, OutboundEntityManager manager) {
        outboundDataSources.put(dataTypeID, manager);
    }

    public static void distributeInboundDeltas(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> inbound, MPPlugin plugin) {
        for (Integer type : inbound.keySet()) {
            Map<Integer, Map<Integer, BaseRecord<?>>> entities = inbound.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            if (manager == null) {
                System.err.println("MANAGER NOT FOUND");
                continue;
            }

            for (Integer instance : entities.keySet()) {
                manager.processDelta(instance, entities.get(instance), plugin);
            }
        }
    }

    public static Map<Integer, Map<Integer, BasePackable>> collectOutboundDeltasSocket() {
        Map<Integer, Map<Integer, BasePackable>> out = new HashMap<>();

        for (Integer source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.SOCKET) {
                Map<Integer, BasePackable> entities = manager.getOutbound();
                out.put(source, entities);
            }
        }

        return out;
    }

    public static Map<Integer, Map<Integer, BasePackable>> collectOutboundDeltasDatagram() {
        Map<Integer, Map<Integer, BasePackable>> out = new HashMap<>();

        for (Integer source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.DATAGRAM) {
                Map<Integer, BasePackable> entities = manager.getOutbound();
                out.put(source, entities);
            }
        }

        return out;
    }

    /**
     * workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static BaseRecord<?> recordFactory(byte typeID) {
        BaseRecord<?> out = recordInstances.get(typeID);
        if (out == null) throw new NullPointerException("No record type found at ID: " + typeID);
        return out;
    }
}
