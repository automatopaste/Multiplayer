package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.net.data.tables.BaseEntityManager;

import java.util.HashMap;
import java.util.Map;

public abstract class MPPlugin extends BaseEveryFrameCombatPlugin {

    public static final String DATA_KEY = "mp_plugin";

    public enum PluginType {
        SERVER,
        CLIENT
    }

    protected final Map<Class<? extends BaseEntityManager>, BaseEntityManager> entityManagers;

    public MPPlugin() {
        entityManagers = new HashMap<>();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put(DATA_KEY, this);
    }

    public abstract PluginType getType();

    public void initEntityManager(BaseEntityManager manager) {
        entityManagers.put(manager.getClass(), manager);
        manager.register();
    }

    public Map<Class<? extends BaseEntityManager>, BaseEntityManager> getEntityManagers() {
        return entityManagers;
    }

    public void removeEntityManager(Class<? extends BaseEntityManager> clazz) {
        entityManagers.remove(clazz);
    }

    protected void updateEntityManagers(float amount) {
        for (BaseEntityManager manager : entityManagers.values()) manager.update(amount, this);
    }
}
