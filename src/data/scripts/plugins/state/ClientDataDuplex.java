package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles packet generation and transferring deltas between threads
 */
public class ClientDataDuplex {
    private final List<APackable> packables;
    private final Map<Integer, APackable> entities;

    public ClientDataDuplex() {
        entities = new HashMap<>();
        packables = new ArrayList<>();

        packables.add(new InputAggregateData(1));
    }

    public Map<Integer, APackable> update() {
        synchronized (entities) {
            return entities;
        }
    }

    public PacketContainer getPacket(int tick) throws IOException {
        return new PacketContainer(new ArrayList<>(packables), tick);
    }

    public void threadUpdate(Map<Integer, APackable> entities) {
        synchronized (this.entities) {
            this.entities.clear();
            this.entities.putAll(entities);
        }
    }
}
