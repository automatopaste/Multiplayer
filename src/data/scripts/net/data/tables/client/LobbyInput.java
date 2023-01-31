package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class LobbyInput implements InboundEntityManager {

    private final short instanceID;
    private LobbyData lobby;

    private String clientPilotedShipID = null;

    public LobbyInput(short instanceID) {
        this.instanceID = instanceID;
        lobby = null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        if (lobby != null) {
            lobby.update(amount, this);
//
//            List<Short> players = lobby.getPlayers();
//            List<String> playerShipIDs = lobby.getPlayerShipIDs();
//
//            for (int i = 0; i < players.size(); i++) {
//                Short playerID = players.get(i);
//
//                if (i + 1 > playerShipIDs.size()) {
//                    break;
//                }
//
//                if (playerID != null && playerID == instanceID) {
//                    String playerShipID = playerShipIDs.get(i);
//
//                    if (!playerShipID.equals(clientPilotedShipID)) {
//                        clientPilotedShipID = playerShipID;
//
//                        plugin.removeEntityManager(PlayerShipOutput.class);
//                        plugin.initEntityManager(new PlayerShipOutput(instanceID, clientPilotedShipID));
//                    }
//                }
//            }
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin, int tick) {
        if (lobby == null) {
            lobby = new LobbyData(instanceID, null, null);

            lobby.destExecute(toProcess, tick);

            lobby.init(plugin, this);
        } else {
            lobby.destExecute(toProcess, tick);
        }
    }

    public LobbyData getLobby() {
        return lobby;
    }
}
