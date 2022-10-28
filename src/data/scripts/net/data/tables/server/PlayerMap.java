package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.lobby.LobbyData;
import data.scripts.net.data.packables.metadata.lobby.LobbyIDs;
import data.scripts.net.data.packables.metadata.player.PlayerData;
import data.scripts.net.data.packables.metadata.player.PlayerDest;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerMap implements InboundEntityManager, OutboundEntityManager {
    private final Map<Short, PlayerDest> players;
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
        PlayerDest data = players.get(instanceID);

        if (data == null) {
            data = new PlayerDest((short) instanceID, toProcess);
            data.init(plugin);
            players.put(instanceID, data);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    @Override
    public Map<Short, BasePackable> getOutbound() {
        Map<Short, BasePackable> out = new HashMap<>();
        out.put((short) -1, lobby);
        return out;
    }

    @Override
    public void update(float amount) {
        for (PlayerDest playerDest : players.values()) {
            playerDest.update(amount);
        }
    }

    public Map<Short, PlayerDest> getPlayers() {
        return players;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerIDs.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(LobbyIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
