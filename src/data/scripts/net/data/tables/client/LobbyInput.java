package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class LobbyInput implements InboundEntityManager {

    private LobbyData lobby;

    public LobbyInput() {
        lobby = null;
    }

    @Override
    public void execute() {
        if (lobby != null) lobby.destExecute();
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        if (lobby == null) {
            lobby = new LobbyData(instanceID, null, null);
            lobby.overwrite(toProcess);

            lobby.init(plugin);
        } else {
            lobby.overwrite(toProcess);
        }
    }

    public LobbyData getLobby() {
        return lobby;
    }
}
