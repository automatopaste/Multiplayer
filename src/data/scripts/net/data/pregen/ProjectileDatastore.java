package data.scripts.net.data.pregen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ProjectileDatastore implements PregenDatastore {

    private final Map<Short, WeaponSpecAPI> generatedWeaponSpecs = new HashMap<>();
    private final Map<Short, MissileSpecAPI> generatedMissileSpecs = new HashMap<>();
    private final Map<Short, ProjectileSpecAPI> generatedProjectileSpecs = new HashMap<>();
    private final Map<String, Short> generatedWeaponIDs = new HashMap<>();
    private final Map<String, WeaponSpecAPI> projectiles = new HashMap<>();
    private final Map<String, WeaponSpecAPI> missiles = new HashMap<>();
    private final Map<String, WeaponSpecAPI> beams = new HashMap<>();
    private final Map<String, MissileSpecAPI> missileSpecs = new HashMap<>();
    private final Map<String, ProjectileSpecAPI> projectileSpecs = new HashMap<>();

    private short index = 0;

    /**
     * Collate weapon and projectile specs
     */
    public void generate(MPPlugin plugin) {
        for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
            Object o = spec.getProjectileSpec();

            generatedWeaponSpecs.put(index, spec);
            generatedWeaponIDs.put(spec.getWeaponId(), index);

            if (o instanceof MissileSpecAPI) {
                generatedMissileSpecs.put(index, (MissileSpecAPI) o);
                missileSpecs.put(spec.getWeaponId(), (MissileSpecAPI) o);
                missiles.put(spec.getWeaponId(), spec);
            } else if (o instanceof ProjectileSpecAPI) {
                generatedProjectileSpecs.put(index, (ProjectileSpecAPI) o);
                projectileSpecs.put(spec.getWeaponId(), (ProjectileSpecAPI) o);
                projectiles.put(spec.getWeaponId(), spec);
            } else if (o == null) {
                beams.put(spec.getWeaponId(), spec);
            }

            index++;
        }
    }

    public Map<Short, WeaponSpecAPI> getGeneratedWeaponSpecs() {
        return generatedWeaponSpecs;
    }

    public Map<String, Short> getGeneratedWeaponIDs() {
        return generatedWeaponIDs;
    }

    public Map<Short, MissileSpecAPI> getGeneratedMissileSpecs() {
        return generatedMissileSpecs;
    }

    public Map<Short, ProjectileSpecAPI> getGeneratedProjectileSpecs() {
        return generatedProjectileSpecs;
    }

    public Map<String, MissileSpecAPI> getMissileSpecs() {
        return missileSpecs;
    }

    public Map<String, ProjectileSpecAPI> getProjectileSpecs() {
        return projectileSpecs;
    }

    public Map<String, WeaponSpecAPI> getBeams() {
        return beams;
    }

    public Map<String, WeaponSpecAPI> getMissiles() {
        return missiles;
    }

    public Map<String, WeaponSpecAPI> getProjectiles() {
        return projectiles;
    }
}
