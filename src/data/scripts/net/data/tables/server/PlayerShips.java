package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShips implements InboundEntityManager {

    // map client connection id to data object
    private final Map<Short, PlayerShipData> playerShips = new HashMap<>();

    // map ship id to client connection id
    private final Map<Short, Short> activeShips = new HashMap<>();

    private final ShipTable shipTable;
    private short hostActiveShipID;

    public PlayerShips(ShipTable shipTable) {
        this.shipTable = shipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.getPlayerShip() == null || engine.getPlayerShip().isShuttlePod()) {
            hostActiveShipID = -1;
        } else {
            try {
                hostActiveShipID = shipTable.getRegistered().get(engine.getPlayerShip());
            } catch (NullPointerException n) {
                Global.getLogger(PlayerShips.class).error("unable to find id for host ship in local table");
            }
        }

        activeShips.clear();

        activeShips.put(hostActiveShipID, DEFAULT_HOST_INSTANCE);

        for (PlayerShipData playerShipData : playerShips.values()) {
            playerShipData.update(amount, this, plugin);

            activeShips.put(playerShipData.getPlayerShipID(), playerShipData.getInstanceID());
        }

        for (PlayerShipData playerShipData : playerShips.values()) {
            short current = playerShipData.getPlayerShipID();
            short requested = playerShipData.getRequestedShipID();
            if (requested != -1) { // remote client is submitting an id to switch to
                boolean transfer = activeShips.containsValue(requested);
                if (transfer) {
                    ShipAPI dest = shipTable.getTable()[requested].getShip();

                    playerShipData.transferPlayerShip(dest);

                    activeShips.remove(current);
                    activeShips.put(requested, playerShipData.getInstanceID());
                }
            }
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
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        PlayerShipData data = playerShips.get(instanceID);

        if (data == null) {
            data = new PlayerShipData(instanceID, null);

            playerShips.put(instanceID, data);

            data.destExecute(toProcess, tick);
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        PlayerShipData data = playerShips.get(instanceID);

        if (data != null) {
            data.delete();

            playerShips.remove(instanceID);
        }
    }

    public Short getHostShipID() {
        return hostActiveShipID;
    }
}
