package data.scripts.net.data;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.server.ServerSendPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketManager {
    public List<Packable> packables;

    public PacketManager() {
        packables = new ArrayList<>();

        packables.add(new ShipData(Global.getCombatEngine().getPlayerShip()));
    }

    public synchronized void update() {

    }

    public ServerSendPacket getPacket() throws IOException {
        return new ServerSendPacket(new ArrayList<>(packables));
    }
}
