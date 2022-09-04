package data.scripts.data;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.ShipVariantData;
import data.scripts.plugins.MPServerPlugin;

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

    private List<ShipVariantData> generated;

    public LoadedDataStore() {
        variantData = new HashMap<>();
    }

    public Map<String, ShipVariantData> getVariantData() {
        return variantData;
    }

    /**
     * Collects data that needs to be loaded on client side before combat entities can be updated or spawned
     * @param engine NNEEEEEOOOOOOOOOOWWW VVvvv VVvv VVvv NEEEEEEEEEEEEOOWOOOWOWOOW Vvv NEEEEEEEEOOOOO
     * @param plugin the plugin
     */
    public void generate(CombatEngineAPI engine, MPServerPlugin plugin) {
        List<FleetMemberAPI> members = new ArrayList<>();

        CombatFleetManagerAPI manager0 = engine.getFleetManager(0);
        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = engine.getFleetManager(0);
        members.addAll(manager1.getDeployedCopy());
        members.addAll(manager1.getReservesCopy());

        generated = new ArrayList<>();
        int index = 0;
        for (FleetMemberAPI member : members) {
            generated.add(new ShipVariantData(index, member.getVariant(), member.getId()));
            index++;
        }
    }

    /**
     * Adds any ship variant data to store
     * @param entities incoming
     */
    public void absorbVariants(Map<Integer, BasePackable> entities) {
        for (BasePackable packable : entities.values()) {
            if (packable instanceof ShipVariantData) {
                ShipVariantData data = (ShipVariantData) packable;
                variantData.put(data.getShipId().getRecord(), data);
            }
        }
    }

    public List<ShipVariantData> getGenerated() {
        return generated;
    }
}
