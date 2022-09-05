package data.scripts.net.data.tables.server;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.trans.PilotCommandData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServerPilotCommandMap implements InboundEntityManager {
    private final Map<Integer, PilotCommandData> inputs;

    public ServerPilotCommandMap() {
        inputs = new HashMap<>();
    }

    @Override
    public void processDelta(int id, BasePackable toProcess, MPPlugin plugin) {
        PilotCommandData delta = (PilotCommandData) toProcess;
        PilotCommandData data = inputs.get(id);

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

    public Map<Integer, PilotCommandData> getInputs() {
        return inputs;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(PilotCommandData.TYPE_ID, this);
    }
}
