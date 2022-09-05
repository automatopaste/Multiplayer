package data.scripts.net.io;

import data.scripts.net.data.BasePackable;

import java.util.*;

/**
 * Manage data between game and network threads
 */
public class DataDuplex {
    /**
     * Map Type ID to
     */
    private final Map<Integer, Map<Integer, BasePackable>> inbound;
    private final Map<Integer, Map<Integer, BasePackable>> outbound;

    public DataDuplex() {
        inbound = new HashMap<>();
        outbound = new HashMap<>();
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public Map<Integer, Map<Integer, BasePackable>> getDeltas() {
        synchronized (inbound) {
            Map<Integer, Map<Integer, BasePackable>> out = new HashMap<>(inbound);
            inbound.clear();
            return out;
        }
    }

    /**
     * Get outbound data and clear store
     * @return outbound entities
     */
    public Map<Integer, Map<Integer, BasePackable>> getOutbound() {
        Map<Integer, Map<Integer, BasePackable>> outEntities;
        synchronized (outbound) {
            outEntities = new HashMap<>(outbound);
            outbound.clear();
        }

        return outEntities;
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateInbound(Map<Integer, Map<Integer, BasePackable>> entities) {
        synchronized (this.inbound) {
            for (Integer type : entities.keySet()) {
                Map<Integer, BasePackable> inboundEntities = inbound.get(type);
                Map<Integer, BasePackable> deltas = entities.get(type);

                for (Integer instance : deltas.keySet()) {
                    BasePackable p = inboundEntities.get(instance);
                    BasePackable d = deltas.get(instance);

                    if (p == null) {
                        inboundEntities.put(instance, d);
                    } else {
                        p.updateFromDelta(d);
                    }
                }
            }
        }
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateOutbound(Map<Integer, Map<Integer, BasePackable>> entities) {
        synchronized (this.outbound) {
            for (Integer type : entities.keySet()) {
                Map<Integer, BasePackable> inboundEntities = outbound.get(type);
                Map<Integer, BasePackable> deltas = entities.get(type);

                for (Integer instance : deltas.keySet()) {
                    BasePackable p = inboundEntities.get(instance);
                    BasePackable d = deltas.get(instance);

                    if (p == null) {
                        inboundEntities.put(instance, d);
                    } else {
                        p.updateFromDelta(d);
                    }
                }
            }
        }
    }
}
