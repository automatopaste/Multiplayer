package data.scripts.net.data;

import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Allows mods to specify entity types and record types at runtime
 */
public class DataGenManager {
    public static Map<Class<? extends EntityData>, Byte> entityTypeIDs = new HashMap<>();

    public static Map<String, Byte> recordTypeIDs = new HashMap<>();
    public static Map<Byte, DataRecord<?>> recordInstances = new HashMap<>();

    public static Map<Byte, InboundEntityManager> inboundDataDestinations = new HashMap<>();
    public static Map<Byte, OutboundEntityManager> outboundDataSources = new HashMap<>();

    private static byte idIncrementer = 1;

    public static byte registerEntityType(Class<? extends EntityData> clazz) {
        byte id = idIncrementer;
        entityTypeIDs.put(clazz, id);
        idIncrementer++;

        return id;
    }

    public static byte registerRecordType(String c, DataRecord<?> instance) {
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

    public static void distributeInboundDeltas(InboundData inbound, MPPlugin plugin, int tick) {
        for (byte type : inbound.in.keySet()) {
            Map<Short, Map<Byte, Object>> entities = inbound.in.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            if (manager == null) {
                System.err.println("MANAGER NOT FOUND");
                continue;
            }

            for (short instance : entities.keySet()) {
                manager.processDelta(type, instance, entities.get(instance), plugin, tick);
            }
        }

        for (byte type : inbound.deleted.keySet()) {
            Set<Short> instances = inbound.deleted.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            if (manager == null) {
                System.err.println("MANAGER NOT FOUND");
                continue;
            }

            for (short instance : instances) {
                manager.processDeletion(type, instance, plugin, tick);
            }
        }
    }

    /**
     * An ordered hierarchy of data that will be compressed into a byte buffer
     * Order: Type ID -> Instance ID -> Record ID
     * @return data hierarchy
     */
    public static OutboundData collectOutboundDeltasSocket(float amount) {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();
        Map<Byte, Set<Short>> deleted = new HashMap<>();

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.SOCKET) {
                Map<Short, InstanceData> entities = manager.getOutbound(source, amount);
                if (entities != null && !entities.isEmpty()) out.put(source, entities);
            }
        }

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.SOCKET) {
                Set<Short> instances = manager.getDeleted(source);
                if (instances != null && !instances.isEmpty()) deleted.put(source, instances);
            }
        }

        return new OutboundData(out, deleted);
    }

    /**
     * An ordered hierarchy of data that will be compressed into a byte buffer
     * Order: Type ID -> Instance ID -> Record ID
     * @return data hierarchy
     */
    public static OutboundData collectOutboundDeltasDatagram(float amount) {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();
        Map<Byte, Set<Short>> deleted = new HashMap<>();

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.DATAGRAM) {
                Map<Short, InstanceData> entities = manager.getOutbound(source, amount);
                if (entities != null && !entities.isEmpty()) out.put(source, entities);
            }
        }

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() == OutboundEntityManager.PacketType.DATAGRAM) {
                Set<Short> instances = manager.getDeleted(source);
                if (instances != null && !instances.isEmpty()) deleted.put(source, instances);
            }
        }

        return new OutboundData(out, deleted);
    }

    /**
     * workaround to avoid reflection
     * @param typeID id
     * @return new empty instance
     */
    public static DataRecord<?> recordFactory(byte typeID) {
        DataRecord<?> out = recordInstances.get(typeID);
        if (out == null) {
            throw new NullPointerException("No record type found at ID: " + typeID);
        }
        return out;
    }
}
