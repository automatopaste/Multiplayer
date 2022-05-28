package data.scripts.net.connection.client;

import data.scripts.net.connection.OutboundEntityManager;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.InputAggregateData;

import java.util.HashMap;
import java.util.Map;

public class ClientInputManager implements OutboundEntityManager {
    private final InputAggregateData input;
    private final int instanceID;

    public ClientInputManager(int instanceID, InputAggregateData input) {
        this.input = input;
        this.instanceID = instanceID;
    }

    @Override
    public Map<Integer, BasePackable> getEntities() {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(instanceID, input);
        return out;
    }

    @Override
    public void update() {

    }
}
