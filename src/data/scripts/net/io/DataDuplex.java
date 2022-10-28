package data.scripts.net.io;

import data.scripts.net.data.packables.BasePackable;
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
    private final Map<Byte, Map<Short, BasePackable>> outboundSocket;
    private final Map<Byte, Map<Short, BasePackable>> outboundDatagram;

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
    public Map<Byte, Map<Short, BasePackable>> getOutboundSocket() {
        Map<Byte, Map<Short, BasePackable>> outEntities;
        synchronized (outboundSocket) {
            outEntities = new HashMap<>(outboundSocket);
            outboundSocket.clear();
        }

        return outEntities;
    }

    public Map<Byte, Map<Short, BasePackable>> getOutboundDatagram() {
        Map<Byte, Map<Short, BasePackable>> outEntities;
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
    public void updateOutboundSocket(Map<Byte, Map<Short, BasePackable>> entities) {
        synchronized (this.outboundSocket) {
            for (byte type : entities.keySet()) {
                Map<Short, BasePackable> outboundEntities = outboundSocket.get(type);
                Map<Short, BasePackable> deltas = entities.get(type);

                if (outboundEntities == null) {
                    outboundEntities = new HashMap<>();
                    outboundSocket.put(type, outboundEntities);
                }

                for (short instance : deltas.keySet()) {
                    BasePackable p = outboundEntities.get(instance);
                    BasePackable d = deltas.get(instance);

                    if (p == null) {
                        outboundEntities.put(instance, d);
                    } else {
                        p.updateFromDelta(d.getRecords());
                    }
                }
            }
        }
    }

    public void updateOutboundDatagram(Map<Byte, Map<Short, BasePackable>> entities) {
        synchronized (this.outboundDatagram) {
            for (byte type : entities.keySet()) {
                Map<Short, BasePackable> outboundEntities = outboundDatagram.get(type);
                Map<Short, BasePackable> deltas = entities.get(type);

                if (outboundEntities == null) {
                    outboundEntities = new HashMap<>();
                    outboundDatagram.put(type, outboundEntities);
                }

                for (short instance : deltas.keySet()) {
                    BasePackable p = outboundEntities.get(instance);
                    BasePackable d = deltas.get(instance);

                    if (p == null) {
                        outboundEntities.put(instance, d);
                    } else {
                        p.updateFromDelta(d.getRecords());
                    }
                }
            }
        }
    }
}
