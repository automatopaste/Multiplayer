package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.datagen.BaseDatagen;

import java.util.HashMap;
import java.util.Map;

public abstract class MPPlugin extends BaseEveryFrameCombatPlugin {

    public static final String DATA_KEY = "mp_plugin";

    public enum PluginType {
        SERVER,
        CLIENT
    }

    protected final Map<Class<? extends BaseEntityManager>, BaseEntityManager> entityManagers = new HashMap<>();
    protected final Map<Class<? extends BaseDatagen>, BaseDatagen> datastores = new HashMap<>();

    public MPPlugin() {

    }

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put(DATA_KEY, this);
    }

    public abstract void stop();

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

    public void initDatastore(BaseDatagen datastore) {
        datastores.put(datastore.getClass(), datastore);
        datastore.generate(this);
    }

    public void genData() {
        for (BaseDatagen datastore : datastores.values()) datastore.generate(this);
    }

    public BaseDatagen getDatastore(Class<? extends BaseDatagen> clazz) {
        return datastores.get(clazz);
    }

    public void removeDatastore(Class<? extends BaseDatagen> clazz) {
        datastores.remove(clazz);
    }
}
