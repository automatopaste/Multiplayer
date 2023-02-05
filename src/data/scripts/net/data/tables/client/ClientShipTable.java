package data.scripts.net.data.tables.client;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.ShipTable;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;

import java.util.HashMap;
import java.util.Map;

public class ClientShipTable extends EntityTable<ShipData> implements InboundEntityManager {

    private final Map<Short, ShieldData> shields;
    private final Map<String, Map<String, MPDefaultAutofireAIPlugin>> tempAutofirePlugins;

    public ClientShipTable() {
        super(new ShipData[ShipTable.MAX_SHIPS]);

        shields = new HashMap<>();
        tempAutofirePlugins = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick) {
        if (typeID == ShipData.TYPE_ID) {
            ShipData data = table[instanceID];

            if (data == null) {
                data = new ShipData(instanceID, null);
                table[instanceID] = data;

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            ShieldData shieldData = shields.get(instanceID);

            if (shieldData == null) {
                ShipData shipData = table[instanceID];
                if (shipData != null && shipData.getShip() != null) {
                    shieldData = new ShieldData(instanceID, shipData.getShip().getShield(), shipData.getShip());
                    shields.put(instanceID, shieldData);

                    shieldData.destExecute(toProcess, tick);

                    shieldData.init(plugin, this);
                }
            } else {
                shieldData.destExecute(toProcess, tick);
            }
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick) {
        ShipData data = table[instanceID];

        if (data != null) {
            data.delete();

            table[instanceID] = null;
            markVacant(instanceID);
        }
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (ShipData ship : table) {
            if (ship != null) {
                ship.update(amount, this);
                ship.interp(amount);
            }
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount, this);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(ShieldData.TYPE_ID, this);
    }

    public Map<Short, ShieldData> getShields() {
        return shields;
    }

    public Map<String, Map<String, MPDefaultAutofireAIPlugin>> getTempAutofirePlugins() {
        return tempAutofirePlugins;
    }
}
