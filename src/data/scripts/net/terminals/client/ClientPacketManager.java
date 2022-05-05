package data.scripts.net.terminals.client;

import data.scripts.net.data.Packable;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.data.packables.InputAggregateData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientPacketManager {
    public List<Packable> packables;

    public ClientPacketManager() {
        packables = new ArrayList<>();

        packables.add(new InputAggregateData());
    }

    public synchronized void update() {

    }

    public PacketContainer getPacket() throws IOException {
        return new PacketContainer(new ArrayList<>(packables));
    }
}
