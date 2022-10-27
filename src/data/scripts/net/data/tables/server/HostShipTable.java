package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.entities.ship.ShipIDs;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HostShipTable extends EntityTable implements OutboundEntityManager {
    public static final int MAX_ENTITIES = 1024;
    private final Map<String, Integer> registered;

    public HostShipTable() {
        registered = new HashMap<>();
    }

    @Override
    public Map<Integer, BasePackable> getOutbound(int entityID) {
        Map<Integer, BasePackable> out = new HashMap<>();

        for (int i = 0; i < table.length; i++) {
            ShipData data = (ShipData) table[i];
            if (data != null) {
                out.put(i, data);
            }
        }

        return out;
    }

    @Override
    public void update(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Set<String> diff = new HashSet<>(registered.keySet());

        for (ShipAPI ship : engine.getShips()) {
            if (registered.containsKey(ship.getId())) {
                diff.remove(ship.getId());
            } else {
                createEntry(ship);
            }
        }

        for (String d : diff) {
            deleteEntry(d);
        }
    }

    private void createEntry(ShipAPI ship) {
        int id = getVacant();

        registered.put(ship.getId(), id);
        table[id] = new ShipData(id, ship);
    }

    private void deleteEntry(String id) {
        int index = registered.get(id);

        table[index] = null;

        registered.remove(id);
        markVacant(index);
    }

    public Map<String, Integer> getRegistered() {
        return registered;
    }

    @Override
    protected int getSize() {
        return MAX_ENTITIES;
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ShipIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DATAGRAM;
    }
}
