package data.scripts.net.data.pregen;

import com.fs.starfarer.loading.o00O;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectileSpecDatastore implements PregenDatastore {

//    private final Map<Short, WeaponSpecAPI> weapons = new HashMap<>();
//    private final Map<Short, MissileSpecAPI> missiles = new HashMap<>();
//    private final Map<Short, ProjectileSpecAPI> projectiles = new HashMap<>();
    private final Map<String, Short> weaponIDs = new HashMap<>();
    private final Map<String, Short> projectileIDs = new HashMap<>();

    /**
     * Collate weapon and projectile specs
     */
    public void generate(MPPlugin plugin) {
        List<String> weaponIDs = new ArrayList<>(o00O.Object()); // obf found in StarfarerSettings.getAllWeaponSpecs();

        short index = 0;
        for (String id : weaponIDs) {
            this.weaponIDs.put(id, index);
            index++;
        }

        List<String> projectileIDs = new ArrayList<>(o00O.o00000()); // educated guess based on weaponIDs

        index = 0;
        for (String id : projectileIDs) {
            this.projectileIDs.put(id, index);
            index++;
        }

//        for (String id : weaponIDs) {
//            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
//            Object o = spec.getProjectileSpec();
//
//            weapons.put(index, spec);
//
//            if (o instanceof MissileSpecAPI) {
//                MissileSpecAPI m = (MissileSpecAPI) o;
//                missiles.put(index, m);
//                String h = m.getHullSpec().getBaseHullId();
//                generatedIDs.put(h, index);
//            } else if (o instanceof ProjectileSpecAPI) {
//                ProjectileSpecAPI s = (ProjectileSpecAPI) o;
//                projectiles.put(index, s);
//                generatedIDs.put(s.getId(), index);
//            } else if (o == null) { // beam
//            }
//
//            index++;
//        }
    }

    public Map<String, Short> getWeaponIDs() {
        return weaponIDs;
    }

    public Map<String, Short> getProjectileIDs() {
        return projectileIDs;
    }
}
