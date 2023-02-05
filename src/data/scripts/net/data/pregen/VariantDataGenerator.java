package data.scripts.net.data.pregen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.net.data.packables.entities.ships.VariantData;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect and package all data that needs to be transferred before remote combat simulation can begin on client
 * e.g. variants
 */
public class VariantDataGenerator implements PregenDatastore {

    private List<VariantData> generated;

    /**
     * Collects data that needs to be loaded on client side before combat entities can be updated or spawned
     */
    public void generate(MPPlugin plugin) {
        List<FleetMemberAPI> members = new ArrayList<>();

        CombatFleetManagerAPI manager0 = Global.getCombatEngine().getFleetManager(0);
        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = Global.getCombatEngine().getFleetManager(1);
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
