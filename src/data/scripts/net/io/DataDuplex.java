package data.scripts.net.io;

import data.scripts.net.data.records.BaseRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage data between game and network threads
 */
public class DataDuplex {
    /**
     * Map Type ID to
     */
    private final Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> inbound;
    private final Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundSocket;
    private final Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundDatagram;

    public DataDuplex() {
        inbound = new HashMap<>();
        outboundSocket = new HashMap<>();
        outboundDatagram = new HashMap<>();
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> getDeltas() {
        synchronized (inbound) {
            HashMap<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> out = new HashMap<>(inbound);
            inbound.clear();
            return out;
        }
    }

    /**
     * Get outbound data and clear store
     * @return outbound entities
     */
    public Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> getOutboundSocket() {
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outEntities;
        synchronized (outboundSocket) {
            outEntities = new HashMap<>(outboundSocket);
            outboundSocket.clear();
        }

        return outEntities;
    }

    public Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> getOutboundDatagram() {
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outEntities;
        synchronized (outboundDatagram) {
            outEntities = new HashMap<>(outboundDatagram);
            outboundDatagram.clear();
        }

        return outEntities;
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateInbound(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities) {
        synchronized (this.inbound) {
            for (Byte type : entities.keySet()) {
                Map<Short, Map<Byte, BaseRecord<?>>> inboundEntities = inbound.get(type);
                Map<Short, Map<Byte, BaseRecord<?>>> deltas = entities.get(type);

                if (inboundEntities == null) {
                    inboundEntities = new HashMap<>();
                    inbound.put(type, inboundEntities);
                }

                for (Short instance : deltas.keySet()) {
                    Map<Byte, BaseRecord<?>> p = inboundEntities.get(instance);
                    Map<Byte, BaseRecord<?>> d = deltas.get(instance);

                    if (p == null) {
                        inboundEntities.put(instance, d);
                    } else {
                        for (Byte k : p.keySet()) {
                            BaseRecord<?> delta = d.get(k);
                            if (delta != null) p.put(k, delta);
                        }
                    }
                }
            }
        }
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateOutboundSocket(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities) {
        synchronized (outboundSocket) {
            updateMap(outboundSocket, entities);
        }
    }

    public void updateOutboundDatagram(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities) {
        synchronized (outboundDatagram) {
            updateMap(outboundDatagram, entities);
        }
    }

    private void updateMap(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> dest, Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> delta) {
        for (byte type : delta.keySet()) {
            Map<Short, Map<Byte, BaseRecord<?>>> outboundEntities = dest.get(type);
            Map<Short, Map<Byte, BaseRecord<?>>> deltas = delta.get(type);

            if (outboundEntities == null) {
                outboundEntities = new HashMap<>();
                dest.put(type, outboundEntities);
            }

            for (short instance : deltas.keySet()) {
                Map<Byte, BaseRecord<?>> outboundEntityRecords = outboundEntities.get(instance);
                Map<Byte, BaseRecord<?>> records = deltas.get(instance);

                if (outboundEntityRecords == null) {
                    outboundEntityRecords = new HashMap<>();
                    outboundEntities.put(instance, outboundEntityRecords);
                }

                for (byte id : records.keySet()) {
                    BaseRecord<?> outboundRecord = outboundEntityRecords.get(id);
                    BaseRecord<?> record = records.get(id);

                    if (outboundRecord == null) {
                        outboundRecord = record;
                        outboundEntityRecords.put(id, outboundRecord);
                    }
                }
            }
        }
    }
}
