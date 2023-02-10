package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ClientData;
import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerLobby implements InboundEntityManager, OutboundEntityManager {
    private final Map<Short, ClientData> players;

    private final ClientData host;
    private final LobbyData lobby;

    public PlayerLobby(MPServerPlugin serverPlugin) {

        players = new HashMap<>();
        host = new ClientData((short) 0, Global.getCombatEngine().getViewport(), serverPlugin);
        players.put((short) 0, host);

        lobby = new LobbyData((short) 0, this, serverPlugin.getPlayerShipMap());
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick) {
        ClientData data = players.get(instanceID);

        if (data == null) {
            data = new ClientData(instanceID, null, null);
            players.put(instanceID, data);

            data.destExecute(toProcess, tick);

            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick) {
        ClientData data = players.get(instanceID);

        if (data != null) {
            data.delete();

            players.remove(instanceID);
        }
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = lobby.sourceExecute(amount);
        if (instanceData.records != null && !instanceData.records.isEmpty()) {
            out.put((short) -1, instanceData);
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID) {
        return null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        host.update(amount, this);
        lobby.update(amount, this);
        for (ClientData clientData : players.values()) {
            clientData.update(amount, this);
        }
    }

    public Map<Short, ClientData> getPlayers() {
        return players;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ClientData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
