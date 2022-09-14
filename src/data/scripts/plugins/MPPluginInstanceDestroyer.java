package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

public class MPPluginInstanceDestroyer extends BaseEveryFrameCombatPlugin {
    @Override
    public void init(CombatEngineAPI engine) {
        //MPModPlugin.destroyPlugin();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        //Global.getCombatEngine().removePlugin(this);
    }
}
