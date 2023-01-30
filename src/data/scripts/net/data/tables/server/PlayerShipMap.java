package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipMap implements InboundEntityManager {

    private final Map<Short, PlayerShipData> playerShips;
//    private final PlayerShipData hostShipData;
//    private ShipAPI hostShip;

    public PlayerShipMap() {
        playerShips = new HashMap<>();
//        hostShipData = new PlayerShipData((short) -1, new SourceExecute<String>() {
//            @Override
//            public String get() {
//                if (hostShip != null) return hostShip.getFleetMemberId();
//                return null;
//            }
//        });
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
//        hostShip = Global.getCombatEngine().getPlayerShip();

        for (PlayerShipData playerShipData : playerShips.values()) {
            playerShipData.update(amount);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    public Map<Short, PlayerShipData> getPlayerShips() {
        return playerShips;
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerShipData data = playerShips.get(instanceID);

        if (data == null) {
            data = new PlayerShipData(instanceID, null);

            playerShips.put(instanceID, data);

            data.destExecute(toProcess);
            data.init(plugin);
        } else {
            data.destExecute(toProcess);
        }
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }

//    @Override
//    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
//        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();
//
//        hostShipData.sourceExecute();
//
//        Map<Byte, BaseRecord<?>> deltas = hostShipData.getDeltas();
//        if (deltas != null && !deltas.isEmpty()) {
//            out.put((short) -1, deltas);
//        }
//
//        return out;
//    }
}
