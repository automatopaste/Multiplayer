package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.packables.entities.ship.ShipDest;
import data.scripts.net.data.packables.entities.ship.ShipIDs;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ClientShipTable extends EntityTable implements InboundEntityManager {
    private ShipAPI clientActive;

    @Override
    public void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        ShipDest data = (ShipDest) table[id];

        if (data == null) {
            ShipDest shipDest = new ShipDest(id, toProcess);
            table[id] = shipDest;
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
        DataGenManager.registerInboundEntityManager(ShipIDs.TYPE_ID, this);
    }

    public void setClientActive(ShipAPI clientActive) {
        this.clientActive = clientActive;
    }

    public ShipAPI getClientActive() {
        return clientActive;
    }
}
