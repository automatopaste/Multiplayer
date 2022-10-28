package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.metadata.lobby.LobbyDest;
import data.scripts.net.data.packables.metadata.lobby.LobbyIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class LobbyInput implements InboundEntityManager {

    private LobbyDest lobby;

    public LobbyInput() {
        lobby = null;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(LobbyIDs.TYPE_ID, this);
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        if (lobby == null) {
            lobby = new LobbyDest(instanceID, toProcess);
            lobby.init(plugin);
        } else {
            lobby.updateFromDelta(toProcess);
        }
    }

    public LobbyDest getLobby() {
        return lobby;
    }
}
