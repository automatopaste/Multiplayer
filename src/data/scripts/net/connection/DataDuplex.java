package data.scripts.net.connection;

import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.*;

/**
 * Instantiate one per connection
 */
public class DataDuplex {
    private final Map<Integer, BasePackable> inbound;
    private final Map<Integer, BasePackable> outbound;

    private int currTick;

    private boolean doFlush;

    public DataDuplex() {
        inbound = new HashMap<>();
        outbound = new HashMap<>();

        doFlush = true;
        currTick = 0;
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
    public PacketContainer getPacket(int tick) throws IOException {
        List<BasePackable> outEntities;
        synchronized (outbound) {
            outEntities = new ArrayList<>(outbound.values());
            outbound.clear();
        }

        PacketContainer p = new PacketContainer(outEntities, tick, doFlush);
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

    public synchronized void setCurrTick(int currTick) {
        this.currTick = currTick;
    }

    public synchronized int getCurrTick() {
        return currTick;
    }
}