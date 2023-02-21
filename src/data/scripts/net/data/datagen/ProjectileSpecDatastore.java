package data.scripts.net.data.datagen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.loading.o00O;
import data.scripts.plugins.MPPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectileSpecDatastore implements BaseDatagen {

//    private final Map<Short, WeaponSpecAPI> weapons = new HashMap<>();
    private final Map<String, MissileSpecAPI> missiles = new HashMap<>();
    private final Map<String, ProjectileSpecAPI> projectiles = new HashMap<>();
    private final Map<String, Short> weaponIDs = new HashMap<>();
    private final Map<Short, String> weaponIDKeys = new HashMap<>();
    private final Map<String, Short> projectileIDs = new HashMap<>();
    private final Map<Short, String> projectileIDKeys = new HashMap<>();
    private final Map<String, String> weaponSpawnIDs = new HashMap<>();

    /**
     * Collate weapon and projectile specs
     */
    public void generate(MPPlugin plugin) {
        List<String> weaponIDs = new ArrayList<>(o00O.Object()); // obf found in StarfarerSettings.getAllWeaponSpecs();

        short index = 0;
        for (String id : weaponIDs) {
            this.weaponIDs.put(id, index);
            this.weaponIDKeys.put(index, id);
            index++;
        }

        List<String> projectileIDs = new ArrayList<>(o00O.o00000()); // educated guess based on weaponIDs

        index = 0;
        for (String id : projectileIDs) {
            this.projectileIDs.put(id, index);
            this.projectileIDKeys.put(index, id);
            index++;
        }

        for (String id : weaponIDs) {
            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
            Object o = spec.getProjectileSpec();

            if (o instanceof MissileSpecAPI) {
                MissileSpecAPI m = (MissileSpecAPI) o;

//                try {
//                    String mirvProjectile = m.getBehaviorJSON().getString("projectileSpec");
//                    float f = 0f;
//                } catch (Exception e) {
//
//                }

                missiles.put(m.getHullSpec().getBaseHullId(), m);
            } else if (o instanceof ProjectileSpecAPI) {
                ProjectileSpecAPI s = (ProjectileSpecAPI) o;
                projectiles.put(s.getId(), s);
            } else if (o == null) { // beam
            }
        }

        SettingsAPI settings = Global.getSettings();
        try {
            JSONArray data = settings.getMergedSpreadsheetDataForMod("projectileID","data/config/mp/proj_spawns.csv", "multiplayer");

            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                String projectileID = row.getString("projectileID");
                String weaponID = row.getString("weaponID");

                weaponSpawnIDs.put(projectileID, weaponID);
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Short> getWeaponIDs() {
        return weaponIDs;
    }

    public Map<Short, String> getWeaponIDKeys() {
        return weaponIDKeys;
    }

    public Map<String, Short> getProjectileIDs() {
        return projectileIDs;
    }

    public Map<Short, String> getProjectileIDKeys() {
        return projectileIDKeys;
    }

    public Map<String, MissileSpecAPI> getMissiles() {
        return missiles;
    }

    public Map<String, ProjectileSpecAPI> getProjectiles() {
        return projectiles;
    }

    public Map<String, String> getWeaponSpawnIDs() {
        return weaponSpawnIDs;
    }
}
