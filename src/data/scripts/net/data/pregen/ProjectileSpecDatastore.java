package data.scripts.net.data.pregen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ProjectileSpecDatastore implements PregenDatastore {

    private final Map<Short, WeaponSpecAPI> weapons = new HashMap<>();
    private final Map<Short, MissileSpecAPI> missiles = new HashMap<>();
    private final Map<Short, ProjectileSpecAPI> projectiles = new HashMap<>();
    private final Map<String, Short> generatedIDs = new HashMap<>();

    private short index = 0;

    /**
     * Collate weapon and projectile specs
     */
    public void generate(MPPlugin plugin) {
        for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
            Object o = spec.getProjectileSpec();

            weapons.put(index, spec);

            if (o instanceof MissileSpecAPI) {
                MissileSpecAPI m = (MissileSpecAPI) o;
                missiles.put(index, m);
                String h = m.getHullSpec().getBaseHullId();
                generatedIDs.put(h, index);
            } else if (o instanceof ProjectileSpecAPI) {
                ProjectileSpecAPI s = (ProjectileSpecAPI) o;
                projectiles.put(index, s);
                generatedIDs.put(s.getId(), index);
            } else if (o == null) { // beam
            }

            index++;
        }
    }

    public Map<Short, WeaponSpecAPI> getWeapons() {
        return weapons;
    }

    public Map<Short, MissileSpecAPI> getMissiles() {
        return missiles;
    }

    public Map<Short, ProjectileSpecAPI> getProjectiles() {
        return projectiles;
    }

    public Map<String, Short> getGeneratedIDs() {
        return generatedIDs;
    }
}
