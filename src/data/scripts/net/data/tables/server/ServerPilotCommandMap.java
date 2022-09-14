package data.scripts.net.data.tables.server;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.packables.metadata.pilot.PilotDest;
import data.scripts.net.data.packables.metadata.pilot.PilotIDs;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServerPilotCommandMap implements InboundEntityManager {
    private final Map<Integer, PilotDest> inputs;

    public ServerPilotCommandMap() {
        inputs = new HashMap<>();
    }

    @Override
    public void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        PilotDest data = inputs.get(id);

        if (data == null) {
            data = new PilotDest(id, toProcess);
            data.init(plugin);
            inputs.put(id, data);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    @Override
    public void update(float amount) {

    }

    public Map<Integer, PilotDest> getInputs() {
        return inputs;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PilotIDs.TYPE_ID, this);
    }
}
