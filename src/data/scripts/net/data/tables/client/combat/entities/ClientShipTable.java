package data.scripts.net.data.tables.client.combat.entities;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;

import java.util.HashMap;
import java.util.Map;

public class ClientShipTable implements InboundEntityManager {

    private final Map<Short, ShipData> registered = new HashMap<>();

    private final Map<Short, ShieldData> shields;
    private final Map<String, Map<String, MPDefaultAutofireAIPlugin>> tempAutofirePlugins;

    public ClientShipTable() {
        shields = new HashMap<>();
        tempAutofirePlugins = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        if (typeID == ShipData.TYPE_ID) {
            ShipData data = registered.get(instanceID);

            if (data == null) {
                data = new ShipData(instanceID, null, null);
                registered.put(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            ShieldData shieldData = shields.get(instanceID);

            if (shieldData == null) {
                ShipData shipData = registered.get(instanceID);
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
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        ShipData data = registered.get(instanceID);

        if (data != null) {
            data.delete();
        }

        registered.remove(instanceID);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (ShipData ship : registered.values()) {
            ship.update(amount, this, plugin);
            ship.interp(amount);
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount, this, plugin);
            shieldData.interp(amount);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(ShieldData.TYPE_ID, this);
    }

    public Map<Short, ShipData> getShips() {
        return registered;
    }

    public Map<Short, ShieldData> getShields() {
        return shields;
    }

    public Map<String, Map<String, MPDefaultAutofireAIPlugin>> getTempAutofirePlugins() {
        return tempAutofirePlugins;
    }
}
