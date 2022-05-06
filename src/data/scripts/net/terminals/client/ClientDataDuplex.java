package data.scripts.net.terminals.client;

import data.scripts.net.data.Packable;
import data.scripts.net.data.RecordDelta;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles packet generation and transferring deltas between threads
 */
public class ClientDataDuplex {
    private final List<Packable> packables;
    private final List<Map<Integer, RecordDelta>> entities;

    public ClientDataDuplex() {
        entities = new ArrayList<>();
        packables = new ArrayList<>();

        packables.add(new InputAggregateData());
    }

    public void update() {

    }

    public PacketContainer getPacket(int tick) throws IOException {
        return new PacketContainer(new ArrayList<>(packables), tick);
    }

    public void threadUpdate(List<Map<Integer, RecordDelta>> entities) {
        synchronized (this.entities) {
            this.entities.clear();
            this.entities.addAll(entities);
        }
    }

    public List<Map<Integer, RecordDelta>> getEntities() {
        synchronized (entities) {
            return entities;
        }
    }
}
