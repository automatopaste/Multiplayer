package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.lobby.LobbyData;
import data.scripts.net.data.packables.metadata.lobby.LobbyIDs;
import data.scripts.net.data.packables.metadata.player.PlayerData;
import data.scripts.net.data.packables.metadata.player.PlayerDest;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipDest;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerMap implements InboundEntityManager, OutboundEntityManager {
    private final Map<Integer, PlayerDest> players;
    private final Map<Integer, PlayerShipDest> playerShips;
    private final MPServerPlugin serverPlugin;

    private final PlayerData host;
    private final PlayerShipData hostShip;
    private final LobbyData lobby;

    public PlayerMap(MPServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;

        players = new HashMap<>();
        playerShips = new HashMap<>();

        host = new PlayerData(-1, Global.getCombatEngine().getViewport(), serverPlugin);
        hostShip = new PlayerShipData(-1, new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return getHostShipID();
            }
        });
        lobby = new LobbyData(-1, this);
    }

    @Override
    public void processDelta(int entityID, int instanceID, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        if (entityID == PlayerIDs.TYPE_ID) {
            PlayerDest data = players.get(instanceID);

            if (data == null) {
                data = new PlayerDest(instanceID, toProcess);
                data.init(plugin);
                players.put(instanceID, data);
            } else {
                data.updateFromDelta(toProcess);
            }
        } else if (entityID == PlayerShipIDs.TYPE_ID) {
            PlayerShipDest data = playerShips.get(instanceID);

            if (data == null) {
                data = new PlayerShipDest(instanceID, toProcess);
                data.init(plugin);
                playerShips.put(instanceID, data);
            } else {
                data.updateFromDelta(toProcess);
            }
        }
    }

    @Override
    public Map<Integer, BasePackable> getOutbound(int entityID) {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(-1, lobby);
        return out;
    }

    @Override
    public void update(float amount) {
        for (PlayerDest playerDest : players.values()) {
            playerDest.update(amount);
        }

        for (PlayerShipDest playerShipDest : playerShips.values()) {
            playerShipDest.update(amount);
        }
    }

    public Map<Integer, PlayerDest> getPlayers() {
        return players;
    }

    public Map<Integer, PlayerShipDest> getPlayerShips() {
        return playerShips;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerIDs.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(PlayerShipIDs.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(LobbyIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }

    public String getHostShipID() {
        return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
    }
}
