package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.entities.ship.ShieldData;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ClientShipTable extends EntityTable<ShipData> implements InboundEntityManager {

    private final Map<Short, ShieldData> shields;

    public ClientShipTable() {
        super(new ShipData[100]);

        shields = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        if (typeID == ShipData.TYPE_ID) {
            ShipData data = table[instanceID];

            if (data == null) {
                data = new ShipData(instanceID, null);
                table[instanceID] = data;

                data.destExecute(toProcess);

                data.init(plugin);
            } else {
                data.destExecute(toProcess);
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            ShieldData shieldData = shields.get(instanceID);

            if (shieldData == null) {
                shieldData = new ShieldData(instanceID, null);
                shields.put(instanceID, shieldData);

                shieldData.destExecute(toProcess);

                shieldData.init(plugin);
            } else {
                shieldData.destExecute(toProcess);
            }
        }
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (ShipData ship : table) {
            if (ship != null) ship.update(amount);
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(ShieldData.TYPE_ID, this);
    }
}
