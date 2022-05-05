package data.scripts.net.terminals.server;

import data.scripts.net.data.Packable;
import data.scripts.net.data.packables.SimpleEntity;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerPacketManager {
    public List<Packable> packables;

    public ServerPacketManager() {
        packables = new ArrayList<>();

//        packables.add(new ShipData(Global.getCombatEngine().getPlayerShip()));
        packables.add(new SimpleEntity());
    }

    public synchronized void update() {

    }

    public PacketContainer getPacket() throws IOException {
        return new PacketContainer(new ArrayList<>(packables));
    }
}
