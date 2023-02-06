package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class ShipTable extends EntityTable<ShipData> implements OutboundEntityManager {

    public static final int MAX_SHIPS = 500;

    private final Map<ShipAPI, Short> registered;
    private final Set<Short> deleted;
    private final Map<Short, ShieldData> shields;
    private final PlayerShips playerShips;

    public ShipTable(PlayerShips playerShips) {
        super(new ShipData[MAX_SHIPS]);
        this.playerShips = playerShips;

        registered = new HashMap<>();
        deleted = new HashSet<>();
        shields = new HashMap<>();
    }

    @Override
    public Map<Short, Map<Byte, DataRecord<?>>> getOutbound(byte typeID, float amount) {
        Map<Short, Map<Byte, DataRecord<?>>> out = new HashMap<>();

        if (typeID == ShipData.TYPE_ID) {
            for (int i = 0; i < table.length; i++) {
                ShipData data = table[i];
                if (data != null) {
                    Map<Byte, DataRecord<?>> deltas = data.sourceExecute(amount);

                    if (deltas != null && !deltas.isEmpty()) {
                        out.put((short) i, deltas);
                    }
                }
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            for (Short id : shields.keySet()) {
                ShieldData shieldData = shields.get(id);

                Map<Byte, DataRecord<?>> deltas = shieldData.sourceExecute(amount);

                if (deltas != null && !deltas.isEmpty()) {
                    out.put(id, deltas);
                }
            }
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID) {
        Set<Short> out = new HashSet<>(deleted);
        deleted.clear();
        return out;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Set<ShipAPI> diff = new HashSet<>(registered.keySet());

        for (ShipAPI ship : engine.getShips()) {
            if (ship.isFighter()) continue;

            if (registered.containsKey(ship)) {
                diff.remove(ship);
            } else {
                createEntry(ship);
            }
        }

        for (ShipAPI d : diff) {
            deleteEntry(d);
        }

        for (ShipData shipData : table) {
            if (shipData != null) shipData.update(amount, this);
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount, this);
        }
    }

    private void createEntry(ShipAPI ship) {
        short id = (short) getVacant();

        registered.put(ship, id);
        table[id] = new ShipData(id, ship, playerShips);
        if (ship.getShield() != null) {
            shields.put(id, new ShieldData(id, ship.getShield(), ship));
        }
    }

    private void deleteEntry(ShipAPI ship) {
        short index = registered.get(ship);

        table[index] = null;

        registered.remove(ship);
        shields.remove(index);
        markVacant(index);

        deleted.add(index);
    }

    public Map<Short, Map<Byte, DataRecord<?>>> getShipsRegistered() {
        Map<Short, Map<Byte, DataRecord<?>>> out = new HashMap<>();

        for (short id : registered.values()) {
            ShipData shipData = table[id];

            shipData.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            List<RecordLambda<?>> recordLambdas = shipData.getRecords();
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
            }

            out.put(id, records);
        }

        return out;
    }

    public Map<ShipAPI, Short> getRegistered() {
        return registered;
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ShieldData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
