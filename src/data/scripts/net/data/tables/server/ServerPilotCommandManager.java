package data.scripts.net.data.tables.server;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.trans.InputAggregateData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServerPilotCommandManager implements InboundEntityManager {
    private final Map<Integer, InputAggregateData> inputs;

    public ServerPilotCommandManager() {
        inputs = new HashMap<>();
    }

    @Override
    public void processDelta(int id, BasePackable toProcess, MPPlugin plugin) {
        InputAggregateData delta = (InputAggregateData) toProcess;
        InputAggregateData data = inputs.get(id);

        if (data == null) {
            delta.destinationInit(plugin);
            inputs.put(id, delta);
        } else {
            data.updateFromDelta(delta);
        }
    }

    @Override
    public void updateEntities() {

    }

    public Map<Integer, InputAggregateData> getInputs() {
        return inputs;
    }
}
