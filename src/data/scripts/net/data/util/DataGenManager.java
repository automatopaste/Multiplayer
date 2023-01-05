package data.scripts.net.data.util;

import data.scripts.net.data.packables.BasePackable;
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

    public static Map<String, Byte> recordTypeIDs = new HashMap<>();
    public static Map<Byte, BaseRecord<?>> recordInstances = new HashMap<>();

    public static Map<Byte, InboundEntityManager> inboundDataDestinations = new HashMap<>();
    public static Map<Byte, OutboundEntityManager> outboundDataSources = new HashMap<>();

    private static byte idIncrementer = 1;

    public static byte registerEntityType(Class<? extends BasePackable> clazz) {
        byte id = idIncrementer;
        entityTypeIDs.put(clazz, id);
        idIncrementer++;
        return id;
    }

    public static byte registerRecordType(String c, BaseRecord<?> instance) {
        byte id = idIncrementer;
        recordTypeIDs.put(c, id);
        recordInstances.put(id, instance);
        idIncrementer++;
        return id;
    }

    public static void registerInboundEntityManager(byte dataTypeID, InboundEntityManager manager) {
        inboundDataDestinations.put(dataTypeID, manager);
    }

    public static void registerOutboundEntityManager(byte dataTypeID, OutboundEntityManager manager) {
        outboundDataSources.put(dataTypeID, manager);
    }

    public static void distributeInboundDeltas(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> inbound, MPPlugin plugin) {
        for (byte type : inbound.keySet()) {
            Map<Short, Map<Byte, BaseRecord<?>>> entities = inbound.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            if (manager == null) {
                System.err.println("MANAGER NOT FOUND");
                continue;
            }

            for (short instance : entities.keySet()) {
                manager.processDelta(instance, entities.get(instance), plugin);
            }
        }
    }

    /**
     * An ordered hierarchy of data that will be compressed into a byte buffer
     * Order: Type ID -> Instance ID -> Record ID
     * @return data hierarchy
     */
    public static Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> collectOutboundDeltasSocket() {
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> out = new HashMap<>();

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.SOCKET) {
                Map<Short, Map<Byte, BaseRecord<?>>> entities = manager.getOutbound();
                out.put(source, entities);
            }
        }

        return out;
    }

    /**
     * An ordered hierarchy of data that will be compressed into a byte buffer
     * Order: Type ID -> Instance ID -> Record ID
     * @return data hierarchy
     */
    public static Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> collectOutboundDeltasDatagram() {
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> out = new HashMap<>();

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.DATAGRAM) {
                Map<Short, Map<Byte, BaseRecord<?>>> entities = manager.getOutbound();
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
