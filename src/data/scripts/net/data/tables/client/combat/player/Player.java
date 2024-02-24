package data.scripts.net.data.tables.client.combat.player;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ClientData;
import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Player implements OutboundEntityManager {
    private final ClientData player;
    private final short connectionID;

    public Player(byte connectionID, MPPlugin plugin) {
        this.connectionID = connectionID;

        String username = Global.getSettings().getString("MP_UsernameString");
        if (username.length() > LobbyData.MAX_USERNAME_CHARS) username = username.substring(0, LobbyData.MAX_USERNAME_CHARS);

        player = new ClientData(connectionID, connectionID, plugin, username);
    }

    @Override
    public Map<Byte, Map<Short, InstanceData>> getOutbound(byte typeID, float amount, List<Byte> connectionIDs) {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();

        Map<Short, InstanceData> connectionOutData = new HashMap<>();

        InstanceData instanceData = player.sourceExecute(amount);
        if (instanceData.records != null && !instanceData.records.isEmpty()) {
            connectionOutData.put(this.connectionID, instanceData);
        }

        for (byte connectionID : connectionIDs) {
            out.put(connectionID, connectionOutData);
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
