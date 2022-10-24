package data.scripts.net.data.tables.server;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.packables.metadata.player.PlayerDest;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServerPlayerMap implements InboundEntityManager {
    private final Map<Integer, PlayerDest> inputs;

    public ServerPlayerMap() {
        inputs = new HashMap<>();
    }

    @Override
    public void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PlayerDest data = inputs.get(id);

        if (data == null) {
            data = new PlayerDest(id, toProcess);
            data.init(plugin);
            inputs.put(id, data);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    @Override
    public void update(float amount) {

    }

    public Map<Integer, PlayerDest> getInputs() {
        return inputs;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PlayerIDs.TYPE_ID, this);
    }
}
