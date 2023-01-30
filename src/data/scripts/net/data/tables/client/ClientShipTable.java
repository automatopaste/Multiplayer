package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ClientShipTable extends EntityTable<ShipData> implements InboundEntityManager {

    public ClientShipTable() {
        super(new ShipData[100]);
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        ShipData data = table[instanceID];

        if (data == null) {
            ShipData shipData = new ShipData(instanceID, null);
            table[instanceID] = shipData;

            shipData.overwrite(toProcess);

            shipData.init(plugin);
        } else {
            data.overwrite(toProcess);
        }
    }

    @Override
    public void execute(MPPlugin plugin) {
        for (BasePackable p : table) {
            if (p != null) {
                p.destExecute();
            }
        }
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (ShipData ship : table) {
            if (ship != null) ship.update(amount);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
    }
}
