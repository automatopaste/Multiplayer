package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.*;

public class DataDuplex {
    private final Map<Integer, APackable> inbound;
    private final Map<Integer, APackable> outbound;
    private final Set<Integer> removedInbound;
    private final Set<Integer> removedOutbound;

    public DataDuplex() {
        inbound = new HashMap<>();
        outbound = new HashMap<>();
        removedInbound = new HashSet<>();
        removedOutbound = new HashSet<>();
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public Map<Integer, APackable> getDeltas() {
        synchronized (inbound) {
            return inbound;
        }
    }

    public Set<Integer> getRemovedInbound() {
        synchronized (removedInbound) {
            return removedInbound;
        }
    }

    /**
     * Create a packet to send over socket connection
     * @param tick tick of current thread
     * @return the packet
     * @throws IOException fuck up
     */
    public PacketContainer getPacket(int tick) throws IOException {
        List<APackable> outEntities;
        synchronized (outbound) {
            outEntities = new ArrayList<>(outbound.values());
            outbound.clear();
        }
        List<Integer> outRemovedInstances;
        synchronized (removedOutbound) {
            outRemovedInstances = new ArrayList<>(removedOutbound);
            removedOutbound.clear();
        }

        return new PacketContainer(outEntities, outRemovedInstances, tick);
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     * @param removed items deleted last tick
     */
    public void updateInbound(Map<Integer, APackable> entities, List<Integer> removed) {
        synchronized (this.inbound) {
            this.inbound.clear();
            this.inbound.putAll(entities);
        }
        synchronized (this.removedInbound) {
            this.removedInbound.clear();
            this.removedInbound.addAll(removed);
        }
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     * @param removed items deleted last tick
     */
    public void updateOutbound(Map<Integer, APackable> entities, List<Integer> removed) {
        synchronized (this.outbound) {
            this.outbound.clear();
            this.outbound.putAll(entities);
        }
        synchronized (this.removedOutbound) {
            this.removedOutbound.clear();
            this.removedOutbound.addAll(removed);
        }
    }
}
