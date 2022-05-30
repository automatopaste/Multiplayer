package data.scripts.net.data;

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
    public Map<Integer, BasePackable> getOutbound() {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(instanceID, input);
        return out;
    }

    @Override
    public void update() {

    }
}
