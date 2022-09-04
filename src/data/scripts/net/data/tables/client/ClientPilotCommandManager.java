package data.scripts.net.data.tables.client;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.trans.InputAggregateData;
import data.scripts.net.data.tables.OutboundEntityManager;

import java.util.HashMap;
import java.util.Map;

public class ClientPilotCommandManager implements OutboundEntityManager {
    private final InputAggregateData input;
    private final int instanceID;

    public ClientPilotCommandManager(int instanceID, InputAggregateData input) {
        this.input = input;
        this.instanceID = instanceID;
    }

    @Override
    public Map<Integer, BasePackable> getOutbound() {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(instanceID, input);
        return out;
    }

    @Override
    public void update() {

    }
}
