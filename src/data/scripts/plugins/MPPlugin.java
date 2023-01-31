package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.plugins.gui.MPUIPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class MPPlugin extends BaseEveryFrameCombatPlugin {
    public enum PluginType {
        SERVER,
        CLIENT
    }

    protected final Map<Class<? extends BaseEntityManager>, BaseEntityManager> entityManagers;

    public MPPlugin() {
        entityManagers = new HashMap<>();

        Global.getCombatEngine().addPlugin(new MPUIPlugin(this));
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
