package data.scripts.plugins.state;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.InputAggregateData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exists to store client control input states and process deltas
 */
public class ServerInputManager extends BaseEveryFrameCombatPlugin {
    private final Map<Integer, Integer> states;

    public ServerInputManager() {
        states = new HashMap<>();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        for (Integer integer : states.values()) {
            InputAggregateData.unmask(integer, Global.getCombatEngine().getPlayerShip());
        }
    }

    public void updateClientInput(int id, InputAggregateData delta) {
        states.put(id, delta.getKeysBitmask().getRecord());
    }

    public Map<Integer, Integer> getStates() {
        return states;
    }
}
