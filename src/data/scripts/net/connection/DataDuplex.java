package data.scripts.net.connection;

import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Manage data between game and network threads
 */
public class DataDuplex {
    private final Map<Integer, BasePackable> inbound;
    private final Map<Integer, BasePackable> outbound;

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
    public Map<Integer, BasePackable> getDeltas() {
        synchronized (inbound) {
            Map<Integer, BasePackable> out = new HashMap<>(inbound);
            inbound.clear();
            return out;
        }
    }

    /**
     * Create a packet to send over socket connection
     * @param tick tick of current thread
     * @return the packet
     * @throws IOException fuck up
     */
    public PacketContainer getPacket(int tick, InetSocketAddress dest) throws IOException {
        List<BasePackable> outEntities;
        synchronized (outbound) {
            outEntities = new ArrayList<>(outbound.values());
            outbound.clear();
        }

        PacketContainer p = new PacketContainer(outEntities, tick, doFlush, dest);
        doFlush = false;
        return p;
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateInbound(Map<Integer, BasePackable> entities) {
        synchronized (this.inbound) {
            for (Integer key : entities.keySet()) {
                BasePackable p = inbound.get(key);
                BasePackable e = entities.get(key);

                if (p != null) {
                    if (p.equals(e)) continue;

                    p.updateFromDelta(e);
                } else {
                    inbound.put(key, e);
                }
            }
        }
    }

    /**
     * Synchronises update of current data store
     * @param entities new entities copy
     */
    public void updateOutbound(Map<Integer, BasePackable> entities) {
        synchronized (this.outbound) {
            for (Integer key : entities.keySet()) {
                BasePackable p = outbound.get(key);
                BasePackable e = entities.get(key);

                if (p != null) {
                    if (p.equals(e)) continue;

                    p.updateFromDelta(e);
                } else {
                    outbound.put(key, e);
                }
            }
        }
    }

    public void flush() {
        doFlush = true;
    }
}
