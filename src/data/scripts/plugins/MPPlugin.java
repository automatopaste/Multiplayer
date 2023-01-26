package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import data.scripts.net.data.tables.BaseEntityManager;

import java.util.HashSet;
import java.util.Set;

public abstract class MPPlugin extends BaseEveryFrameCombatPlugin {
    public enum PluginType {
        SERVER,
        CLIENT
    }

    protected final Set<BaseEntityManager> entityManagers;

    public MPPlugin() {
        entityManagers = new HashSet<>();
    }

    public abstract PluginType getType();

    public void initEntityManager(BaseEntityManager manager) {
        entityManagers.add(manager);
        manager.register();
    }

    public Set<BaseEntityManager> getEntityManagers() {
        return entityManagers;
    }

    protected void updateEntityManagers(float amount) {
        for (BaseEntityManager manager : entityManagers) manager.update(amount, this);
    }
}
