package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.ShipData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

public class ClientShipTable extends EntityTable implements InboundEntityManager {
    private ShipAPI clientActive;

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

        CombatEngineAPI engine = Global.getCombatEngine();
        if (clientActive != null && engine.getPlayerShip() != null && clientActive != engine.getPlayerShip()) {
            engine.setPlayerShipExternal(clientActive);
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

    public void setClientActive(ShipAPI clientActive) {
        this.clientActive = clientActive;
    }

    public ShipAPI getClientActive() {
        return clientActive;
    }
}
