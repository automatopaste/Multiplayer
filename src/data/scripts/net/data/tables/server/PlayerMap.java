package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.packables.metadata.PlayerData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerMap implements InboundEntityManager, OutboundEntityManager {
    private final Map<Short, PlayerData> players;

    private final PlayerData host;
    private final LobbyData lobby;

    public PlayerMap(MPServerPlugin serverPlugin) {

        players = new HashMap<>();
        host = new PlayerData((short) 0, Global.getCombatEngine().getViewport(), serverPlugin);
        players.put((short) 0, host);

        lobby = new LobbyData((short) 0, this, serverPlugin.getPlayerShipMap());
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerData data = players.get(instanceID);

        if (data == null) {
            data = new PlayerData(instanceID, null, null);
            players.put(instanceID, data);

            data.destExecute(toProcess);

            data.init(plugin, this);
        } else {
            data.destExecute(toProcess);
        }
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        Map<Byte, BaseRecord<?>> deltas = lobby.sourceExecute();
        if (deltas != null && !deltas.isEmpty()) {
            out.put((short) -1, deltas);
        }

        return out;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        host.update(amount, this);
        lobby.update(amount, this);
        for (PlayerData playerData : players.values()) {
            playerData.update(amount, this);
        }
    }

    public Map<Short, PlayerData> getPlayers() {
        return players;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
