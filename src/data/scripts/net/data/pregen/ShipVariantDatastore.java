package data.scripts.net.data.pregen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.net.data.packables.entities.ships.VariantData;
import data.scripts.plugins.MPPlugin;

import java.util.*;

/**
 * Collect and package all data that needs to be transferred before remote combat simulation can begin on client
 * e.g. variants
 */
public class ShipVariantDatastore implements PregenDatastore {

    private final Map<String, VariantData> generated = new HashMap<>();
    private short index = 0;

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

        for (FleetMemberAPI member : members) {
            generated.put(member.getId(), initData(member));
        }
    }

    public Map<String, VariantData> getGenerated() {
        return generated;
    }

    public void checkVariantUpdate() {
        Set<String> keys = new HashSet<>(generated.keySet());

        List<FleetMemberAPI> members = new ArrayList<>();

        CombatFleetManagerAPI manager0 = Global.getCombatEngine().getFleetManager(0);
        members.addAll(manager0.getDeployedCopy());
        members.addAll(manager0.getReservesCopy());

        CombatFleetManagerAPI manager1 = Global.getCombatEngine().getFleetManager(1);
        members.addAll(manager1.getDeployedCopy());
        members.addAll(manager1.getReservesCopy());

        for (FleetMemberAPI member : members) {
            if (!keys.remove(member.getId())) {
                generated.put(member.getId(), initData(member));
            }
        }
    }

    private VariantData initData(FleetMemberAPI member) {
        index++;
        return new VariantData(index, member.getVariant(), member.getId());
    }
}
