package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.PlayerData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerOutput implements OutboundEntityManager {
    private final PlayerData player;
    private final short instanceID;

    public PlayerOutput(short instanceID, MPPlugin plugin) {
        this.instanceID = instanceID;

        player = new PlayerData(instanceID, Global.getCombatEngine().getViewport(), plugin);
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();
        out.put(instanceID, player.getDeltas());
        return out;
    }

    @Override
    public void execute(MPPlugin plugin) {
        player.sourceExecute();
    }

    @Override
    public void update(float amount, MPPlugin plugin) {

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
