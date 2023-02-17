package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ClientData;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Player implements OutboundEntityManager {
    private final ClientData player;
    private final short connectionID;

    public Player(byte connectionID, MPPlugin plugin) {
        this.connectionID = connectionID;

        String username = Global.getSettings().getString("MP_UsernameString");
        player = new ClientData(connectionID, connectionID, plugin, username);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = player.sourceExecute(amount);
        if (instanceData.records != null && !instanceData.records.isEmpty()) {
            out.put(this.connectionID, instanceData);
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID, byte connectionID) {
        return null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        player.update(amount, this, plugin);
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ClientData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
