package data.scripts.data;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.loading.ShipVariantData;
import data.scripts.plugins.mpServerPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collect and package all data that needs to be transferred before combat simulation can begin on client
 * e.g. variants
 */
public class DataPreloadTransferManager {
    public static Map<Integer, BasePackable> get(CombatEngineAPI engine, mpServerPlugin plugin) {
        CombatFleetManagerAPI manager0 = engine.getFleetManager(0);

        List<FleetMemberAPI> members = new ArrayList<>();

        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = engine.getFleetManager(0);
        members.addAll(manager1.getDeployedCopy());
        members.addAll(manager1.getReservesCopy());

        Map<Integer, BasePackable> data = new HashMap<>();

        for (FleetMemberAPI member : members) {
            int id = plugin.getNewInstanceID(null);
            ShipVariantData variantData = new ShipVariantData(id, member.getVariant(), member.getId());

            data.put(id, variantData);
        }

        return data;
    }
}
