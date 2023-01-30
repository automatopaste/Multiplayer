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

    private String id = null;

    public LobbyInput(short instanceID) {
        this.instanceID = instanceID;
        lobby = null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        if (lobby != null) {
            short[] players = lobby.getPlayers();

            for (int i = 0; i < players.length; i++) {
                short s = players[i];

                if (s == instanceID) {
                    String e = lobby.getPlayerShipIDs().get(i);

                    if (!e.equals(id)) {
                        id = e;

                        plugin.initEntityManager(new PlayerShipOutput(instanceID, id));
                    }
                }
            }
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        if (lobby == null) {
            lobby = new LobbyData(instanceID, null, null);

            lobby.destExecute(toProcess);

            lobby.init(plugin, this);
        } else {
            lobby.destExecute(toProcess);
        }
    }

    public LobbyData getLobby() {
        return lobby;
    }
}
