package data.scripts.net.data.tables.server.combat.entities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.combat.entities.BallisticProjectile;
import com.fs.starfarer.combat.entities.DamagingExplosion;
import com.fs.starfarer.combat.entities.Missile;
import com.fs.starfarer.combat.entities.MovingRay;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.projectiles.BallisticProjectileData;
import data.scripts.net.data.packables.entities.projectiles.MissileData;
import data.scripts.net.data.packables.entities.projectiles.MovingRayData;
import data.scripts.net.data.datagen.ProjectileSpecDatastore;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.EntityInstanceMap;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class ProjectileTable implements OutboundEntityManager {

    public static final int MAX_PROJECTILES = 2000;

    private final EntityInstanceMap<MovingRayData> movingRays = new EntityInstanceMap<>();
    private final EntityInstanceMap<BallisticProjectileData> ballisticProjectiles = new EntityInstanceMap<>();
    private final EntityInstanceMap<MissileData> missiles = new EntityInstanceMap<>();

    private final Map<DamagingProjectileAPI, Short> registered = new HashMap<>();
    private final Set<Short> deleted;
    private final ProjectileSpecDatastore datastore;
    private final ShipTable shipTable;

    private final Queue<Short> vacant;

    public ProjectileTable(ProjectileSpecDatastore datastore, ShipTable shipTable) {
        this.datastore = datastore;
        this.shipTable = shipTable;

        deleted = new HashSet<>();

        vacant = new LinkedList<>();
        for (short i = 0; i < MAX_PROJECTILES; i++) {
            vacant.add(i);
        }
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Set<DamagingProjectileAPI> diff = new HashSet<>(registered.keySet());

        for (DamagingProjectileAPI projectile : engine.getProjectiles()) {
            if (projectile instanceof DamagingExplosion) continue;

            if (registered.containsKey(projectile)) {
                diff.remove(projectile);
            } else {
                createEntry(projectile);
            }
        }

        for (DamagingProjectileAPI d : diff) {
            deleteEntry(d);
        }

        for (MovingRayData r : movingRays.registered.values()) {
            r.update(amount, this, plugin);
        }
        for (BallisticProjectileData r : ballisticProjectiles.registered.values()) {
            r.update(amount, this, plugin);
        }
        for (MissileData r : missiles.registered.values()) {
            r.update(amount, this, plugin);
        }
    }

    private void createEntry(DamagingProjectileAPI projectile) {
        Short id = vacant.poll();
        assert id != null;

        registered.put(projectile, id);
        String projectileID = projectile.getProjectileSpecId();

        String w = datastore.getWeaponSpawnIDs().get(projectileID);
        short weaponSpecID;
        if (w != null) {
            weaponSpecID = datastore.getWeaponIDs().get(w);
        } else {
            weaponSpecID = datastore.getWeaponIDs().get(projectile.getWeapon().getSpec().getWeaponId());
        }

        if (projectile instanceof MovingRay) {
            movingRays.registered.put(id, new MovingRayData(id, (MovingRay) projectile, weaponSpecID, shipTable));
            return;
        }
        if (projectile instanceof BallisticProjectile) {
            ballisticProjectiles.registered.put(id, new BallisticProjectileData(id, (BallisticProjectile) projectile, weaponSpecID, shipTable));
            return;
        }
        if (projectile instanceof Missile) {
            missiles.registered.put(id, new MissileData(id, (Missile) projectile, weaponSpecID, shipTable));
            return;
        }
    }

    private void deleteEntry(DamagingProjectileAPI projectile) {
        short index = registered.get(projectile);
        registered.remove(projectile);

        vacant.add(index);

        if (projectile instanceof MovingRay) {
            movingRays.delete(index);
            return;
        }
        if (projectile instanceof BallisticProjectile) {
            ballisticProjectiles.delete(index);
            return;
        }
        if (projectile instanceof Missile) {
            missiles.delete(index);
            return;
        }
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(MovingRayData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(BallisticProjectileData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(MissileData.TYPE_ID, this);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        if (typeID == MovingRayData.TYPE_ID) {
            for (MovingRayData movingRayData : movingRays.registered.values()) {
                InstanceData instanceData = movingRayData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(movingRayData.getInstanceID(), instanceData);
                }
            }
        } else if (typeID == BallisticProjectileData.TYPE_ID) {
            for (BallisticProjectileData ballisticProjectileData : ballisticProjectiles.registered.values()) {
                InstanceData instanceData = ballisticProjectileData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(ballisticProjectileData.getInstanceID(), instanceData);
                }
            }
        } else if (typeID == MissileData.TYPE_ID) {
            for (MissileData missileData : missiles.registered.values()) {
                InstanceData instanceData = missileData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(missileData.getInstanceID(), instanceData);
                }
            }
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID, byte connectionID) {
        if (typeID == MovingRayData.TYPE_ID) {
            return movingRays.getDeleted();
        } else if (typeID == BallisticProjectileData.TYPE_ID) {
            return ballisticProjectiles.getDeleted();
        } else if (typeID == MissileData.TYPE_ID) {
            return missiles.getDeleted();
        }
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public Map<Byte, Map<Short, InstanceData>> getProjectilesRegistered() {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();

        Map<Short, InstanceData> r = new HashMap<>();
        out.put(MovingRayData.TYPE_ID, r);
        for (short id : movingRays.registered.keySet()) {
            MovingRayData e = movingRays.registered.get(id);
            e.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            List<RecordLambda<?>> recordLambdas = e.getRecords();
            int size = 0;
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }

            r.put(id, new InstanceData(size, records));
        }

        Map<Short, InstanceData> b = new HashMap<>();
        out.put(BallisticProjectileData.TYPE_ID, b);
        for (short id : ballisticProjectiles.registered.keySet()) {
            BallisticProjectileData e = ballisticProjectiles.registered.get(id);
            e.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            List<RecordLambda<?>> recordLambdas = e.getRecords();
            int size = 0;
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }

            b.put(id, new InstanceData(size, records));
        }

        Map<Short, InstanceData> m = new HashMap<>();
        out.put(MissileData.TYPE_ID, m);
        for (short id : missiles.registered.keySet()) {
            MissileData e = missiles.registered.get(id);
            e.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            List<RecordLambda<?>> recordLambdas = e.getRecords();
            int size = 0;
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }

            m.put(id, new InstanceData(size, records));
        }

        return out;
    }
}
