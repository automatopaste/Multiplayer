package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import data.scripts.net.data.packables.APackable;

import java.util.HashMap;

public class EntityRecordManager extends BaseEveryFrameCombatPlugin {
    private HashMap<String, APackable> entities = new HashMap<>();
    public EntityRecordManager() {

    }
}
