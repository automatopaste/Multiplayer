package data.scripts.net.terminals.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.Packable;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles packet generation and transferring deltas to main thread plugin
 */
public class ServerDataDuplex {
    private final List<Packable> packables;

    public ServerDataDuplex() {
        packables = new ArrayList<>();

        packables.add(new ShipData(Global.getCombatEngine().getPlayerShip()));
//        packables.add(new SimpleEntity());
    }

    public synchronized void update() {

    }

    public PacketContainer getPacket(int tick) throws IOException {
        return new PacketContainer(new ArrayList<>(packables), tick);
    }
}
