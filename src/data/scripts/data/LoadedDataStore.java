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
 * Collect and package all data that needs to be transferred before remote combat simulation can begin on client
 * e.g. variants
 */
public class LoadedDataStore {
    private final Map<String, ShipVariantData> variantData;

    public LoadedDataStore(Map<Integer, BasePackable> incoming) {
        variantData = new HashMap<>();

        for (BasePackable packable : incoming.values()) {
            if (packable instanceof ShipVariantData) {
                ShipVariantData variant = (ShipVariantData) packable;
                variantData.put(variant.getShipId().getRecord(), variant);
            }
        }
    }

    public Map<String, ShipVariantData> getVariantData() {
        return variantData;
    }

    /**
     * Collects data that needs to be loaded on client side before combat entities can be updated or spawned
     * @param engine NNEEEEEOOOOOOOOOOWWW VVvvv VVvv VVvv NEEEEEEEEEEEEOOWOOOWOWOOW Vvv NEEEEEEEEOOOOO
     * @param plugin the plugin
     * @return entities to load
     */
    public static Map<Integer, BasePackable> generate(CombatEngineAPI engine, mpServerPlugin plugin) {
        List<FleetMemberAPI> members = new ArrayList<>();

        CombatFleetManagerAPI manager0 = engine.getFleetManager(0);
        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = engine.getFleetManager(0);
        members.addAll(manager1.getDeployedCopy());
        members.addAll(manager1.getReservesCopy());

        Map<Integer, BasePackable> data = new HashMap<>();

        for (FleetMemberAPI member : members) {
            int id = plugin.getNewInstanceID();
            ShipVariantData variantData = new ShipVariantData(id, member.getVariant(), member.getId());

            data.put(id, variantData);
        }

        return data;
    }
}
