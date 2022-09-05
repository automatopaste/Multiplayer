package data.scripts.net.data.tables.client;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.ShipData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

public class ClientShipTable extends EntityTable implements InboundEntityManager {
//    @Override
//    public void processDeltas(Map<Integer, BasePackable> toProcess) {
//        for (Integer id : toProcess.keySet()) {
//            ShipData delta = (ShipData) toProcess.get(id);
//            ShipData data = (ShipData) table[id];
//
//            if (data == null) {
//                table[id] = delta;
//                delta.destinationInit(clientPlugin);
//            } else {
//                data.updateFromDelta(delta);
//            }
//        }
//    }

    @Override
    public void processDelta(int id, BasePackable toProcess, MPPlugin plugin) {
        ShipData delta = (ShipData) toProcess;
        ShipData data = (ShipData) table[id];

        if (data == null) {
            table[id] = delta;
            delta.destinationInit(plugin);
        } else {
            data.updateFromDelta(delta);
        }
    }

    @Override
    public void updateEntities() {
        for (BasePackable p : table) {
            if (p != null) p.destinationUpdate();
        }
    }

    @Override
    protected int getSize() {
        return ServerShipTable.MAX_ENTITIES;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
    }
}
