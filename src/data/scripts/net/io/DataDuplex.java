package data.scripts.net.io;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage data between game and network threads
 */
public class DataDuplex {
    /**
     * Map Type ID to
     */
    private final Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> inbound;
    private final Map<Integer, Map<Integer, SourcePackable>> outboundSocket;
    private final Map<Integer, Map<Integer, SourcePackable>> outboundDatagram;

    public DataDuplex() {
        inbound = new HashMap<>();
        outboundSocket = new HashMap<>();
        outboundDatagram = new HashMap<>();
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> getDeltas() {
        synchronized (inbound) {
            Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> out = new HashMap<>(inbound);
            inbound.clear();
            return out;
        }
    }

    /**
     * Get outbound data and clear store
     * @return outbound entities
     */
    public Map<Integer, Map<Integer, SourcePackable>> getOutboundSocket() {
        Map<Integer, Map<Integer, SourcePackable>> outEntities;
        synchronized (outboundSocket) {
            outEntities = new HashMap<>(outboundSocket);
            outboundSocket.clear();
        }

        return outEntities;
    }

    public Map<Integer, Map<Integer, SourcePackable>> getOutboundDatagram() {
        Map<Integer, Map<Integer, SourcePackable>> outEntities;
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
    public void updateInbound(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities) {
        synchronized (this.inbound) {
            for (Integer type : entities.keySet()) {
                Map<Integer, Map<Integer, BaseRecord<?>>> inboundEntities = inbound.get(type);
                Map<Integer, Map<Integer, BaseRecord<?>>> deltas = entities.get(type);

                if (inboundEntities == null) {
                    inboundEntities = new HashMap<>();
                    inbound.put(type, inboundEntities);
                }

                for (Integer instance : deltas.keySet()) {
                    Map<Integer, BaseRecord<?>> p = inboundEntities.get(instance);
                    Map<Integer, BaseRecord<?>> d = deltas.get(instance);

                    if (p == null) {
                        inboundEntities.put(instance, d);
                    } else {
                        for (int k : p.keySet()) {
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
    public void updateOutboundSocket(Map<Integer, Map<Integer, SourcePackable>> entities) {
        synchronized (this.outboundSocket) {
            for (Integer type : entities.keySet()) {
                Map<Integer, SourcePackable> outboundEntities = outboundSocket.get(type);
                Map<Integer, SourcePackable> deltas = entities.get(type);

                if (outboundEntities == null) {
                    outboundEntities = new HashMap<>();
                    outboundSocket.put(type, outboundEntities);
                }

                for (Integer instance : deltas.keySet()) {
                    SourcePackable p = outboundEntities.get(instance);
                    SourcePackable d = deltas.get(instance);

                    if (p == null) {
                        outboundEntities.put(instance, d);
                    } else {
                        p.updateFromDelta(d.getRecords());
                    }
                }
            }
        }
    }

    public void updateOutboundDatagram(Map<Integer, Map<Integer, SourcePackable>> entities) {
        synchronized (this.outboundDatagram) {
            for (Integer type : entities.keySet()) {
                Map<Integer, SourcePackable> outboundEntities = outboundDatagram.get(type);
                Map<Integer, SourcePackable> deltas = entities.get(type);

                if (outboundEntities == null) {
                    outboundEntities = new HashMap<>();
                    outboundDatagram.put(type, outboundEntities);
                }

                for (Integer instance : deltas.keySet()) {
                    SourcePackable p = outboundEntities.get(instance);
                    SourcePackable d = deltas.get(instance);

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
