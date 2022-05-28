package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.*;

public class DataDuplex {
    private final Map<Integer, APackable> inbound;
    private final Map<Integer, APackable> outbound;

    private boolean doFlush;

    public DataDuplex() {
        inbound = new HashMap<>();
        outbound = new HashMap<>();

        doFlush = true;
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public Map<Integer, APackable> getDeltas() {
        synchronized (inbound) {
            Map<Integer, APackable> out = new HashMap<>(inbound);
            inbound.clear();
            return out;
        }
    }
//
//    public Set<Integer> getRemovedInbound() {
//        synchronized (removedInbound) {
//            Set<Integer> out = new HashSet<>(removedInbound);
//            removedInbound.clear();
//            return out;
//        }
//    }

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
//        synchronized (removedOutbound) {
//            outRemovedInstances = new ArrayList<>(removedOutbound);
//            removedOutbound.clear();
//        }

        PacketContainer p = new PacketContainer(outEntities, tick, doFlush);
        doFlush = false;
        return p;
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateInbound(Map<Integer, APackable> entities) {
        synchronized (this.inbound) {
            for (Integer key : entities.keySet()) {
                APackable p = inbound.get(key);
                APackable e = entities.get(key);

                if (p != null) {
                    if (p.equals(e)) continue;

                    p.updateFromDelta(e);
                } else {
                    inbound.put(key, e);
                }
            }
        }
//        synchronized (this.removedInbound) {
//            this.removedInbound.addAll(removed);
//        }
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateOutbound(Map<Integer, APackable> entities) {
        synchronized (this.outbound) {
            for (Integer key : entities.keySet()) {
                APackable p = outbound.get(key);
                APackable e = entities.get(key);

                if (p != null) {
                    if (p.equals(e)) continue;

                    p.updateFromDelta(e);
                } else {
                    outbound.put(key, e);
                }
            }
        }
//        synchronized (this.removedOutbound) {
//            this.removedOutbound.addAll(removed);
//        }
    }

    public void flush() {
        doFlush = true;
    }
}
