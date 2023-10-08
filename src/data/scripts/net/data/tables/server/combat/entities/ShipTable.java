package data.scripts.net.data.tables.server.combat.entities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.entities.ships.WeaponData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.EntityInstanceMap;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.tables.server.combat.players.PlayerShips;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;

import java.util.*;

public class ShipTable extends EntityTable<ShipData> implements OutboundEntityManager {

    public static final int MAX_SHIPS = 500;

    private final Map<ShipAPI, Short> registered;
    private final Set<Short> deleted;
    private final EntityInstanceMap<ShieldData> shields;
    private final EntityInstanceMap<WeaponData> weapons;
    private final MPServerPlugin serverPlugin;

    public ShipTable(MPServerPlugin serverPlugin) {
        super(new ShipData[MAX_SHIPS]);
        this.serverPlugin = serverPlugin;

        registered = new HashMap<>();
        deleted = new HashSet<>();
        shields = new EntityInstanceMap<>();
        weapons = new EntityInstanceMap<>();
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        if (typeID == ShipData.TYPE_ID) {
            for (int i = 0; i < table.length; i++) {
                ShipData data = table[i];
                if (data != null) {
                    InstanceData instanceData = data.sourceExecute(amount);

                    if (instanceData.records != null && !instanceData.records.isEmpty()) {
                        out.put((short) i, instanceData);
                    }
                }
            }
        } else if (typeID == ShieldData.TYPE_ID) {
            for (Short id : shields.registered.keySet()) {
                ShieldData shieldData = shields.registered.get(id);

                InstanceData instanceData = shieldData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(id, instanceData);
                }
            }
        } else if (typeID == WeaponData.TYPE_ID) {
            for (short id : weapons.registered.keySet()) {
                WeaponData weaponData = weapons.registered.get(id);

                InstanceData instanceData = weaponData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(id, instanceData);
                }
            }
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID, byte connectionID) {
        if (typeID == ShipData.TYPE_ID) {
            Set<Short> out = new HashSet<>(deleted);
            deleted.clear();
            return out;
        } else if (typeID == ShieldData.TYPE_ID) {
            return shields.getDeleted();
        } else if (typeID == WeaponData.TYPE_ID) {
            return weapons.getDeleted();
        }
        return null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Set<ShipAPI> diff = new HashSet<>(registered.keySet());

        for (ShipAPI ship : engine.getShips()) {
            if (ship.isFighter()) continue;

            if (registered.containsKey(ship)) {
                diff.remove(ship);
            } else {
                createEntry(ship);
            }
        }

        for (ShipAPI d : diff) {
            deleteEntry(d);
        }

        for (ShipData shipData : table) {
            if (shipData != null) shipData.update(amount, this, plugin);
        }
        for (ShieldData shieldData : shields.registered.values()) {
            shieldData.update(amount, this, plugin);
        }
        for (WeaponData weaponData : weapons.registered.values()) {
            weaponData.update(amount, this, plugin);
        }
    }

    private void createEntry(ShipAPI ship) {
        short id = (short) getVacant();

        registered.put(ship, id);
        PlayerShips playerShips = (PlayerShips) serverPlugin.getEntityManagers().get(PlayerShips.class);
        ShipData shipData = new ShipData(id, ship, playerShips);
        table[id] = shipData;

        if (ship.getShield() != null) {
            shields.registered.put(id, new ShieldData(id, ship.getShield(), ship));
        }

        weapons.registered.put(id, new WeaponData(id, ship, shipData.getSlotIDs(), shipData.getWeaponSlots()));
    }

    private void deleteEntry(ShipAPI ship) {
        short index = registered.get(ship);

        table[index] = null;

        registered.remove(ship);
        if (ship.getShield() != null) shields.delete(index);
        weapons.delete(index);
        markVacant(index);

        deleted.add(index);
    }

    public Map<Short, InstanceData> getShipsRegistered() {
        Map<Short, InstanceData> out = new HashMap<>();

        for (short id : registered.values()) {
            ShipData shipData = table[id];

            shipData.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            int size = 0;
            List<RecordLambda<?>> recordLambdas = shipData.getRecords();
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }

            out.put(id, new InstanceData(size, records));
        }

        return out;
    }

    public Map<ShipAPI, Short> getRegistered() {
        return registered;
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ShipData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ShieldData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(WeaponData.TYPE_ID, this);
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}