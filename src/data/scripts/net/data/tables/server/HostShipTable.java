package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class HostShipTable extends EntityTable<ShipData> implements OutboundEntityManager {
    public static final int MAX_ENTITIES = Short.MAX_VALUE;
    private final Map<String, Short> registered;

    public HostShipTable() {
        super(new ShipData[100]);

        registered = new HashMap<>();
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        for (int i = 0; i < table.length; i++) {
            ShipData data = table[i];
            if (data != null) {
                Map<Byte, BaseRecord<?>> deltas = data.getDeltas();
                if (deltas != null && !deltas.isEmpty()) {
                    out.put((short) i, deltas);
                }
            }
        }

        return out;
    }

    @Override
    public void execute(MPPlugin plugin) {
        for (BasePackable p : table) if (p != null) p.sourceExecute();
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
    }

    private void createEntry(ShipAPI ship) {
        short id = (short) getVacant();

        registered.put(ship.getId(), id);
        table[id] = new ShipData(id, ship);
    }

    private void deleteEntry(String id) {
        short index = registered.get(id);

        table[index] = null;

        registered.remove(id);
        markVacant(index);
    }

    public Map<Short, Map<Byte, BaseRecord<?>>> getShipsRegistered() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        for (short id : registered.values()) {
            ShipData shipData = table[id];

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
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
