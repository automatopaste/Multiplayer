package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
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
    private final MPServerPlugin serverPlugin;

    private final PlayerData host;
    private final LobbyData lobby;

    public PlayerMap(MPServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;

        players = new HashMap<>();

        host = new PlayerData((short) -1, Global.getCombatEngine().getViewport(), serverPlugin);

        lobby = new LobbyData((short) -1, this, serverPlugin.getPlayerShipMap());
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerData data = players.get(instanceID);

        if (data == null) {
            data = new PlayerData(instanceID, null, null);
            data.overwrite(toProcess);

            data.init(plugin);
            players.put(instanceID, data);
        } else {
            data.overwrite(toProcess);
        }
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();
        out.put((short) -1, lobby.getDeltas());
        return out;
    }

    @Override
    public void execute() {
        host.sourceExecute();
        lobby.sourceExecute();

        for (BasePackable p : players.values()) if (p != null) p.destExecute();
    }

    @Override
    public void update(float amount) {
        host.update(amount);
        for (PlayerData playerData : players.values()) {
            playerData.update(amount);
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
