package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;

public abstract class MPPlugin extends BaseEveryFrameCombatPlugin {
    public enum PluginType {
        SERVER,
        CLIENT
    }

    public abstract PluginType getType();
}
