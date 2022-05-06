package data.scripts.net.terminals.client;

import data.scripts.net.data.Packable;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles packet generation and transferring deltas to main thread plugin
 */
public class ClientDataDuplex {
    public List<Packable> packables;

    public ClientDataDuplex() {
        packables = new ArrayList<>();

        packables.add(new InputAggregateData());
    }

    public void update() {

    }

    public void clientThreadUpdate(List<List<ARecord>> entities) {

    }

    public PacketContainer getPacket(int clientTick) throws IOException {
        return new PacketContainer(new ArrayList<>(packables), clientTick);
    }
}
