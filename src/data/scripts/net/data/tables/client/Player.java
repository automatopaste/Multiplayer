package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.PlayerData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class Player implements OutboundEntityManager {
    private final PlayerData player;
    private final short instanceID;

    public Player(short instanceID, MPPlugin plugin) {
        this.instanceID = instanceID;

        player = new PlayerData(instanceID, Global.getCombatEngine().getViewport(), plugin);
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();


        Map<Byte, BaseRecord<?>> deltas = player.sourceExecute();
        if (deltas != null && !deltas.isEmpty()) {
            out.put(instanceID, deltas);
        }
        return out;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        player.update(amount, this);
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
