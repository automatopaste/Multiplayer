package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.entities.ship.ShipDest;
import data.scripts.net.data.packables.entities.ship.ShipIDs;
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
        ShipDest data = (ShipDest) table[instanceID];

        if (data == null) {
            ShipDest shipDest = new ShipDest(instanceID, toProcess);
            table[instanceID] = shipDest;
            shipDest.init(plugin);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    @Override
    public void update(float amount) {
        for (BasePackable p : table) {
            ShipDest ship = (ShipDest) p;
            if (ship != null) ship.update(amount);
        }
    }

    @Override
    protected int getSize() {
        return HostShipTable.MAX_ENTITIES;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipIDs.TYPE_ID, this);
    }
}
