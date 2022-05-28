package data.scripts.plugins.state;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.data.packables.ShipVariantData;
import data.scripts.plugins.mpServerPlugin;

import java.util.*;

public class ServerCombatEntityManager implements OutboundEntityManager {
    private final Map<Integer, ShipData> ships;
    private final Map<Integer, APackable> consumable;

    private final Map<Integer, Integer> shipToVariants;

    private final mpServerPlugin serverPlugin;

    public ServerCombatEntityManager(mpServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;

        consumable = new HashMap<>();

        ships = new HashMap<>();
        shipToVariants = new HashMap<>();

        initShips(Global.getCombatEngine().getShips());
    }

    /**
     * Update data
     */
    public void update() {
        CombatEngineAPI engine = Global.getCombatEngine();

        List<ShipAPI> engineShips = new ArrayList<>(engine.getShips());

        // update collections
        for (Iterator<ShipAPI> iterator = engineShips.iterator(); iterator.hasNext();) {
            ShipAPI ship = iterator.next();

            if (ships.containsKey(ship.getFleetMemberId().hashCode())) {
                iterator.remove();
            }
            /* Replace inefficient code with faster string hashcode (probability of collision is unlikely)
            for (ShipData data : ships.values()) {
                if (data.getShip().equals(ship)) {
                    iterator.remove();
                }
            }*/
        }

        List<Integer> shipsToRem = new ArrayList<>();
        List<Integer> variantsToRem = new ArrayList<>();
        for (Integer key : ships.keySet()) {
            if (!engine.isEntityInPlay(ships.get(key).getShip())) {
                shipsToRem.add(key);
            }
        }
        for (Integer key : shipsToRem) {
            ships.remove(key);

            int variantKey = shipToVariants.get(key);
            variantsToRem.add(variantKey);
            consumable.remove(variantKey);
            shipToVariants.remove(key);
        }
        //List<Integer> out = new ArrayList<>(shipsToRem);
        //out.addAll(variantsToRem);

        initShips(engineShips);
    }

    private void initShips(List<ShipAPI> toInit) {
        for (ShipAPI ship : toInit) {
            int id = serverPlugin.getNewInstanceID(ship);
            ShipData data = new ShipData(id);
            data.setShip(ship);

            ships.put(id, data);

            int id2 = serverPlugin.getNewInstanceID(null);
            ShipVariantData variantData = new ShipVariantData(id2, ship, ship.getFleetMemberId());
            consumable.put(id2, variantData);

            shipToVariants.put(id, id2);
        }
    }

    public Map<Integer, APackable> getEntities() {
        Map<Integer, APackable> out = new HashMap<Integer, APackable>(ships);

        out.putAll(consumable);
        consumable.clear();

        return out;
    }
}
