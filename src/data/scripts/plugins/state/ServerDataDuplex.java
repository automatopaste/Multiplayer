package data.scripts.plugins.state;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles packet generation and transferring deltas between threads
 */
public class ServerDataDuplex {
    private final List<APackable> packables;
    private final Map<Integer, APackable> entities;

    public ServerDataDuplex() {
        entities = new HashMap<>();
        packables = new ArrayList<>();

        ShipData data = new ShipData(1);
        data.setShip(Global.getCombatEngine().getPlayerShip());

        packables.add(data);
//        packables.add(new SimpleEntity());
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
