package data.scripts.net.data.tables.server.combat.players;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ClientData;
import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.*;

public class PlayerLobby implements InboundEntityManager, OutboundEntityManager {

    public static final byte HOST_ID = Byte.MIN_VALUE;
    public static final short HOST_INSTANCE = Short.MIN_VALUE;

    private final Map<Short, ClientData> players;
    private final ClientData host;

    private final LobbyData lobby;

    private final Map<Byte, String> usernames = new HashMap<>();

    public PlayerLobby(MPServerPlugin serverPlugin, PlayerShips playerShips) {
        players = new HashMap<>();

        String hostUsername = Global.getSettings().getString("MP_UsernameString");
        if (hostUsername.length() > LobbyData.MAX_USERNAME_CHARS) hostUsername = hostUsername.substring(0, LobbyData.MAX_USERNAME_CHARS);
        host = new ClientData(HOST_INSTANCE, HOST_ID, serverPlugin, hostUsername);
        players.put(HOST_INSTANCE, host);

        usernames.put(HOST_ID, hostUsername);

        lobby = new LobbyData(HOST_INSTANCE, this, playerShips);
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        ClientData data = players.get(instanceID);

        if (data == null) {
            data = new ClientData(instanceID, connectionID, null, null);
            players.put(instanceID, data);

            data.destExecute(toProcess, tick);

            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        ClientData data = players.get(instanceID);

        if (data != null) {
            data.delete();

            players.remove(instanceID);
        }
    }

    @Override
    public Map<Byte, Map<Short, InstanceData>> getOutbound(byte typeID, float amount, List<Byte> connectionIDs) {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();

        Map<Short, InstanceData> connectionOutData = new HashMap<>();

        InstanceData instanceData = lobby.sourceExecute(amount);
        if (instanceData.records != null && !instanceData.records.isEmpty()) {
            connectionOutData.put(HOST_INSTANCE, instanceData);
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
        host.update(amount, this, plugin);
        lobby.update(amount, this, plugin);
        for (ClientData clientData : players.values()) {
            clientData.update(amount, this, plugin);

            if (clientData.getUsername() != null) {
                usernames.put(clientData.getConnectionID(), clientData.getUsername());
            }
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

    public Map<Byte, String> getUsernames() {
        return usernames;
    }
}
