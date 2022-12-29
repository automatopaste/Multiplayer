package data.scripts.net.data.util;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.net.data.packables.entities.variant.VariantData;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect and package all data that needs to be transferred before remote combat simulation can begin on client
 * e.g. variants
 */
public class VariantDataGenerator {

    private List<VariantData> generated;

    /**
     * Collects data that needs to be loaded on client side before combat entities can be updated or spawned
     * @param engine NNEEEEEOOOOOOOOOOWWW VVvvv VVvv VVvv NEEEEEEEEEEEEOOWOOOWOWOOW Vvv NEEEEEEEEOOOOO
     */
    public void generate(CombatEngineAPI engine) {
        List<FleetMemberAPI> members = new ArrayList<>();

        CombatFleetManagerAPI manager0 = engine.getFleetManager(0);
        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = engine.getFleetManager(1);
        members.addAll(manager1.getDeployedCopy());
        members.addAll(manager1.getReservesCopy());

        generated = new ArrayList<>();
        short index = 0;
        for (FleetMemberAPI member : members) {
            generated.add(new VariantData(index, member.getVariant(), member.getId()));
            index++;
        }
    }

    public List<VariantData> getGenerated() {
        return generated;
    }
}
