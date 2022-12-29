package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.SourceLambda;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipMap implements InboundEntityManager {

    private final Map<Short, PlayerShipData> playerShips;
    private final PlayerShipData hostShipData;
    private ShipAPI hostShip;

    public PlayerShipMap() {
        playerShips = new HashMap<>();
        hostShipData = new PlayerShipData((short) -1, new SourceLambda<String>() {
            @Override
            public String get() {
                if (hostShip != null) return hostShip.getFleetMemberId();
                return null;
            }
        });
    }

    @Override
    public void execute() {
        hostShipData.execute();
        for (BasePackable p : playerShips.values()) p.execute();
    }

    @Override
    public void update(float amount) {
        hostShip = Global.getCombatEngine().getPlayerShip();

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
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerShipData data = playerShips.get(instanceID);

        if (data == null) {
            data = new PlayerShipData(instanceID, null);
            data.overwrite(toProcess);

            data.init(plugin);
            playerShips.put(instanceID, data);
        } else {
            data.overwrite(toProcess);
        }
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }
}