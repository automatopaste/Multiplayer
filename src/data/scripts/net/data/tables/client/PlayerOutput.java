package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.player.PlayerData;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerOutput implements OutboundEntityManager {
    private final PlayerData player;
    private final int instanceID;

    public PlayerOutput(int instanceID, MPPlugin plugin) {
        this.instanceID = instanceID;

        player = new PlayerData(instanceID, Global.getCombatEngine().getViewport(), plugin);
    }

    @Override
    public Map<Integer, BasePackable> getOutbound() {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(instanceID, player);
        return out;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
