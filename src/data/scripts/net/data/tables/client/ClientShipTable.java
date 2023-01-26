package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.HostShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ClientShipTable extends EntityTable implements InboundEntityManager {

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        ShipData data = (ShipData) table[instanceID];

        if (data == null) {
            ShipData shipData = new ShipData(instanceID, null);
            table[instanceID] = shipData;

            shipData.overwrite(toProcess);
        } else {
            data.overwrite(toProcess);
        }
    }

    @Override
    public void execute(MPPlugin plugin) {
        for (BasePackable p : table) {
            if (p != null) {
                p.destExecute();

                if (p.isInit()) {
                    p.init(plugin);
                    p.setInit(false);
                }
            }
        }
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (BasePackable p : table) {
            ShipData ship = (ShipData) p;
            if (ship != null) ship.update(amount);
        }
    }

    @Override
    protected int getSize() {
        return HostShipTable.MAX_ENTITIES;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
    }
}
