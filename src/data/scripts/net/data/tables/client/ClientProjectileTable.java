package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientProjectileTable implements InboundEntityManager {

    private final Map<Short, ProjectileData> projectiles = new HashMap<>();

    public ClientProjectileTable() {
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        Set<DamagingProjectileAPI> p = new HashSet<>(Global.getCombatEngine().getProjectiles());

        for (ProjectileData data : projectiles.values()) {
            if (data != null) {
                data.update(amount, this);
                data.interp(amount);

                p.remove(data.getProjectile());
            }
        }

        for (DamagingProjectileAPI proj : p) {
            Global.getCombatEngine().removeEntity(proj);
        }
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick) {
        if (typeID == ProjectileData.TYPE_ID) {
            ProjectileData data = projectiles.get(instanceID);

            if (data == null) {
                data = new ProjectileData(instanceID, null, (short) -1, null);
                projectiles.put(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick) {
        ProjectileData data = projectiles.get(instanceID);

        if (data != null) {
            data.delete();

            projectiles.remove(instanceID);
        }
    }


    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ProjectileData.TYPE_ID, this);
    }
}
