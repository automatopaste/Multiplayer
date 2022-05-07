package data.scripts.plugins.state;

import data.scripts.net.data.packables.InputAggregateData;

import java.util.HashMap;
import java.util.Map;

/**
 * Exists to store client control input states and process deltas
 */
public class ServerInputManager {
    private final Map<Integer, Integer> states;

    public ServerInputManager() {
        states = new HashMap<>();
    }

    public void updateClientInput(int id, InputAggregateData delta) {
        states.put(id, delta.getKeysBitmask().getRecord());
    }

    public Map<Integer, Integer> getStates() {
        return states;
    }
}
