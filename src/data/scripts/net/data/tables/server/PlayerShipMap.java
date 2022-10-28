package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipDest;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipMap implements InboundEntityManager {

    private final Map<Short, PlayerShipDest> playerShips;
    private final PlayerShipData hostShip;

    public PlayerShipMap() {
        playerShips = new HashMap<>();
        hostShip = new PlayerShipData((short) -1, new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return getHostShipID();
            }
        });
    }

    @Override
    public void update(float amount) {
        for (PlayerShipDest playerShipDest : playerShips.values()) {
            playerShipDest.update(amount);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerShipIDs.TYPE_ID, this);
    }

    public Map<Short, PlayerShipDest> getPlayerShips() {
        return playerShips;
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerShipDest data = playerShips.get(instanceID);

        if (data == null) {
            data = new PlayerShipDest(instanceID, toProcess);
            data.init(plugin);
            playerShips.put(instanceID, data);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }

}
