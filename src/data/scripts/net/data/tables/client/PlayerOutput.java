package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.packables.metadata.player.PlayerData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerOutput implements OutboundEntityManager {
    private final PlayerData player;
    private final PlayerShipData playerShip;
    private final int instanceID;

    public PlayerOutput(int instanceID, MPPlugin plugin) {
        this.instanceID = instanceID;

        player = new PlayerData(instanceID, Global.getCombatEngine().getViewport(), plugin);
        playerShip = new PlayerShipData(instanceID, new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
            }
        });
    }

    @Override
    public Map<Integer, BasePackable> getOutbound(int entityID) {
        Map<Integer, BasePackable> out = new HashMap<>();
        if (entityID == PlayerIDs.TYPE_ID) {
            out.put(instanceID, player);
        } else {
            out.put(instanceID, playerShip);
        }
        return out;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerIDs.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(PlayerShipIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DATAGRAM;
    }
}
