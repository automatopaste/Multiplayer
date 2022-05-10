package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.InputAggregateData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientInputManager implements OutboundEntityManager {
    private final InputAggregateData input;
    private final int instanceID;

    public ClientInputManager(int instanceID, InputAggregateData input) {
        this.input = input;
        this.instanceID = instanceID;
    }

    @Override
    public Map<Integer, APackable> getEntities() {
        Map<Integer, APackable> out = new HashMap<>();
        out.put(instanceID, input);
        return out;
    }

    @Override
    public List<Integer> updateAndGetRemovedEntityInstanceIds() {
        return null;
    }
}
