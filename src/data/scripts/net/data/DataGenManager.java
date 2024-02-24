package data.scripts.net.data;

import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.List;
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

    public static void distributeInboundDeltas(InboundData inbound, MPPlugin plugin, int tick, byte connectionID) {
        for (byte type : inbound.in.keySet()) {
            Map<Short, Map<Byte, Object>> entities = inbound.in.get(type);
            InboundEntityManager manager = inboundDataDestinations.get(type);

            if (manager == null) {
                System.err.println("MANAGER NOT FOUND");
                continue;
            }

            for (short instance : entities.keySet()) {
                manager.processDelta(type, instance, entities.get(instance), plugin, tick, connectionID);
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
                manager.processDeletion(type, instance, plugin, tick, connectionID);
            }
        }
    }

    /**
     * An ordered hierarchy of data that will be compressed into a byte buffer
     * Order: Type ID -> Instance ID -> Record ID
     * @return data hierarchy
     */
    public static Map<Byte, OutboundData> collectOutboundDeltas(float amount, List<Byte> connectionIDs, OutboundEntityManager.PacketType type) {
        Map<Byte, OutboundData> connectionOutData = new HashMap<>();

        Map<Byte, Map<Byte, Map<Short, InstanceData>>> out = new HashMap<>();
        Map<Byte, Map<Byte, Set<Short>>> deleted = new HashMap<>();

        for (byte connectionID : connectionIDs) {
            out.put(connectionID, new HashMap<Byte, Map<Short, InstanceData>>());
            deleted.put(connectionID, new HashMap<Byte, Set<Short>>());
        }

        for (byte source : outboundDataSources.keySet()) {
            OutboundEntityManager manager = outboundDataSources.get(source);
            if (manager.getOutboundPacketType() != type) continue;

            Map<Byte, Map<Short, InstanceData>> connectionEntities = manager.getOutbound(source, amount, connectionIDs);

            for (byte connectionID : connectionEntities.keySet()) {
                Map<Short, InstanceData> entities = connectionEntities.get(connectionID);
                if (entities == null || entities.isEmpty()) continue;

                Map<Byte, Map<Short, InstanceData>> sourceEntityData = out.get(connectionID);
                sourceEntityData.put(source, entities);
            }

            for (byte connectionID : connectionIDs) {
                Set<Short> deletedEntities = manager.getDeleted(source, connectionID);
                if (deletedEntities == null || deletedEntities.isEmpty()) continue;

                Map<Byte, Set<Short>> deletedEntityData = deleted.get(connectionID);
                deletedEntityData.put(source, deletedEntities);
            }
        }

        for (byte connectionID : connectionIDs) {
            Map<Byte, Map<Short, InstanceData>> connectionSourceEntities = out.get(connectionID);
            Map<Byte, Set<Short>> connectionDeletedEntities = deleted.get(connectionID);

            connectionOutData.put(connectionID, new OutboundData(connectionSourceEntities, connectionDeletedEntities, connectionID));
        }

        return connectionOutData;
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
