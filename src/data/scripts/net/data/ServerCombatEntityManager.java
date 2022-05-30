package data.scripts.net.data;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.ShipData;
import data.scripts.plugins.mpServerPlugin;

import java.util.*;

public class ServerCombatEntityManager implements OutboundEntityManager {
    private final Map<Integer, ShipData> ships;
    private final Map<Integer, BasePackable> consumable;

    private final mpServerPlugin serverPlugin;

    public ServerCombatEntityManager(mpServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;

        consumable = new HashMap<>();

        ships = new HashMap<>();

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

            // fingers crossed :)
            if (ships.containsKey(ship.getFleetMemberId().hashCode())) {
                iterator.remove();
            }
        }

        List<Integer> shipsToRem = new ArrayList<>();
        for (Integer key : ships.keySet()) {
            if (!engine.isEntityInPlay(ships.get(key).getShip())) {
                shipsToRem.add(key);
            }
        }
        for (Integer key : shipsToRem) {
            ships.remove(key);
        }

        initShips(engineShips);
    }

    private void initShips(List<ShipAPI> toInit) {
        for (ShipAPI ship : toInit) {
            int id = serverPlugin.getNewInstanceID(ship);
            ShipData data = new ShipData(id);
            data.setShip(ship);

            ships.put(id, data);
        }
    }

    public Map<Integer, BasePackable> getOutbound() {
        Map<Integer, BasePackable> out = new HashMap<Integer, BasePackable>(ships);

        out.putAll(consumable);
        consumable.clear();

        return out;
    }
}
