package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShips implements InboundEntityManager {

    private final Map<Short, PlayerShipData> playerShips;

    public PlayerShips() {
        playerShips = new HashMap<>();
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (PlayerShipData playerShipData : playerShips.values()) {
            playerShipData.update(amount, this);
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
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess);
        }
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }
}
