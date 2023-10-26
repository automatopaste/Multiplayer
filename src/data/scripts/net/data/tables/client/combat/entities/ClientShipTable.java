package data.scripts.net.data.tables.client.combat.entities;

import cmu.CMUtils;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.entities.ships.WeaponData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.combat.entities.ShipTable;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;

import java.util.HashMap;
import java.util.Map;

public class ClientShipTable implements InboundEntityManager {

    private final EntityTable<ShipData> shipTable;

    private final Map<ShipAPI, Short> shipIDs = new HashMap<>();

    private final Map<Short, ShieldData> shields;
    private final Map<Short, WeaponData> weapons;

    private final Map<String, Map<String, MPDefaultAutofireAIPlugin>> tempAutofirePlugins;

    public ClientShipTable() {
        shipTable = new EntityTable<>(new ShipData[ShipTable.MAX_SHIPS]);

        shields = new HashMap<>();
        weapons = new HashMap<>();
        tempAutofirePlugins = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        if (typeID == ShipData.TYPE_ID) {
            ShipData data = shipTable.array()[instanceID];

            if (data == null) {
                data = new ShipData( null, null);
                data.setInstanceID(instanceID);

                shipTable.set(instanceID, data);

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            ShieldData shieldData = shields.get(instanceID);

            if (shieldData == null) {
                ShipData shipData = shipTable.array()[instanceID];
                if (shipData != null && shipData.getShip() != null) {
                    shieldData = new ShieldData(instanceID, shipData.getShip().getShield(), shipData.getShip());
                    shields.put(instanceID, shieldData);

                    shieldData.destExecute(toProcess, tick);

                    shieldData.init(plugin, this);
                }
            } else {
                shieldData.destExecute(toProcess, tick);
            }
        } else if (typeID == WeaponData.TYPE_ID) {
            WeaponData weaponData = weapons.get(instanceID);

            if (weaponData == null) {
                ShipData shipData = shipTable.array()[instanceID];
                if (shipData != null && shipData.getShip() != null) {
                    weaponData = new WeaponData(instanceID, shipData.getShip(), shipData.getSlotIDs(), shipData.getWeaponSlots());
                    weapons.put(instanceID, weaponData);

                    weaponData.destExecute(toProcess, tick);

                    weaponData.init(plugin, this);
                }
            } else {
                weaponData.destExecute(toProcess, tick);
            }
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        ShipData data = shipTable.array()[instanceID];

        if (data != null) {
            data.delete();

            ShipAPI ship = data.getShip();
            if (ship != null) {
                shipIDs.remove(ship);
            }
        }

        shipTable.remove(instanceID);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (int i = 0; i < shipTable.limit; i++) {
            ShipData ship = shipTable.array()[i];

            if (ship == null) continue;

            ship.update(amount, this, plugin);
            ship.interp(amount);

            ShipAPI s = ship.getShip();
            if (s != null) {
                shipIDs.put(s, ship.getInstanceID());
            }
        }
        for (ShieldData shieldData : shields.values()) {
            shieldData.update(amount, this, plugin);
            shieldData.interp(amount);
        }
        for (WeaponData weaponData : weapons.values()) {
            weaponData.update(amount, this, plugin);
            weaponData.interp(amount);
        }

        // debug
        for (int i = 0; i < shipTable.limit; i++) {
            ShipData data = shipTable.array()[i];
            String text = "null";
            if (data != null && data.getShip() != null) {
                text = data.getShip().getHullSpec().getHullName();
            }
            CMUtils.getGuiDebug().putText(ClientShipTable.class, "k" + i, i + ": " + text);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(ShieldData.TYPE_ID, this);
        DataGenManager.registerInboundEntityManager(WeaponData.TYPE_ID, this);
    }

    public EntityTable<ShipData> getShipTable() {
        return shipTable;
    }

    public Map<ShipAPI, Short> getShipIDs() {
        return shipIDs;
    }

    public Map<Short, ShieldData> getShields() {
        return shields;
    }

    public Map<Short, WeaponData> getWeapons() {
        return weapons;
    }

    public Map<String, Map<String, MPDefaultAutofireAIPlugin>> getTempAutofirePlugins() {
        return tempAutofirePlugins;
    }
}
