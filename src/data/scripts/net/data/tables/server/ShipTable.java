package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class ShipTable extends EntityTable<ShipData> implements OutboundEntityManager {
    private final Map<String, Short> registered;
    private final Map<Short, ShieldData> shields;

    public ShipTable() {
        super(new ShipData[100]);

        registered = new HashMap<>();
        shields = new HashMap<>();
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        if (typeID == ShipData.TYPE_ID) {
            for (int i = 0; i < table.length; i++) {
                ShipData data = table[i];
                if (data != null) {
                    Map<Byte, BaseRecord<?>> deltas = data.sourceExecute();

                    if (deltas != null && !deltas.isEmpty()) {
                        out.put((short) i, deltas);
                    }
                }
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            for (Short id : shields.keySet()) {
                ShieldData shieldData = shields.get(id);

                Map<Byte, BaseRecord<?>> deltas = shieldData.sourceExecute();

                if (deltas != null && !deltas.isEmpty()) {
                    out.put(id, deltas);
                }
            }
        }

        return out;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
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

        for (ShipData shipData : table) {
            if (shipData != null) shipData.update(amount, this);
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount, this);
        }
    }

    private void createEntry(ShipAPI ship) {
        short id = (short) getVacant();

        registered.put(ship.getId(), id);
        table[id] = new ShipData(id, ship);
        if (ship.getShield() != null) {
            shields.put(id, new ShieldData(id, ship.getShield()));
        }
    }

    private void deleteEntry(String id) {
        short index = registered.get(id);

        table[index] = null;

        registered.remove(id);
        shields.remove(index);
        markVacant(index);
    }

    public Map<Short, Map<Byte, BaseRecord<?>>> getShipsRegistered() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        for (short id : registered.values()) {
            ShipData shipData = table[id];

            shipData.sourceExecute();

            Map<Byte, BaseRecord<?>> records = new HashMap<>();
            List<RecordLambda<?>> recordLambdas = shipData.getRecords();
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
            }

            out.put(id, records);
        }

        return out;
    }

    public Map<String, Short> getRegistered() {
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
