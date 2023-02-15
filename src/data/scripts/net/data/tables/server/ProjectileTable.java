package data.scripts.net.data.tables.server;

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
import data.scripts.net.data.packables.entities.projectiles.RayProjectileData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class ProjectileTable implements OutboundEntityManager {

    public static final int MAX_PROJECTILES = 2000;

    private final Map<Short, RayProjectileData> movingRays = new HashMap<>();
    private final Map<Short, BallisticProjectileData> ballisticProjectiles = new HashMap<>();
    private final Map<Short, MissileData> missiles = new HashMap<>();

    private final Map<DamagingProjectileAPI, Short> registered = new HashMap<>();
    private final Map<String, Short> specIDs;
    private final Set<Short> deleted;
    private final ShipTable shipTable;

    private final Queue<Short> vacant;

    public ProjectileTable(Map<String, Short> specIDs, ShipTable shipTable) {
        this.specIDs = specIDs;
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

        for (RayProjectileData r : movingRays.values()) {
            r.update(amount, this);
        }
        for (BallisticProjectileData r : ballisticProjectiles.values()) {
            r.update(amount, this);
        }
        for (MissileData r : missiles.values()) {
            r.update(amount, this);
        }
    }

    private void createEntry(DamagingProjectileAPI projectile) {
        Short id = vacant.poll();
        assert id != null;

        registered.put(projectile, id);
        String projectileID = projectile.getProjectileSpecId();
        short s = specIDs.get(projectileID);

        if (projectile instanceof MovingRay) {
            movingRays.put(id, new RayProjectileData(id, (MovingRay) projectile, s, shipTable));
            return;
        }
        if (projectile instanceof BallisticProjectile) {
            ballisticProjectiles.put(id, new BallisticProjectileData(id, (BallisticProjectile) projectile, s, shipTable));
            return;
        }
        if (projectile instanceof Missile) {
            missiles.put(id, new MissileData(id, (Missile) projectile, s, shipTable));
            return;
        }
    }

    private void deleteEntry(DamagingProjectileAPI projectile) {
        short index = registered.get(projectile);
        registered.remove(projectile);

        vacant.add(index);

        deleted.add(index);

        if (projectile instanceof MovingRay) {
            movingRays.remove(index);
            return;
        }
        if (projectile instanceof BallisticProjectile) {
            ballisticProjectiles.remove(index);
            return;
        }
        if (projectile instanceof Missile) {
            missiles.remove(index);
            return;
        }
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(RayProjectileData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(BallisticProjectileData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(MissileData.TYPE_ID, this);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        if (typeID == RayProjectileData.TYPE_ID) {
            for (RayProjectileData rayProjectileData : movingRays.values()) {
                InstanceData instanceData = rayProjectileData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(rayProjectileData.getInstanceID(), instanceData);
                }
            }
        } else if (typeID == BallisticProjectileData.TYPE_ID) {
            for (BallisticProjectileData ballisticProjectileData : ballisticProjectiles.values()) {
                InstanceData instanceData = ballisticProjectileData.sourceExecute(amount);

                if (instanceData.records != null && !instanceData.records.isEmpty()) {
                    out.put(ballisticProjectileData.getInstanceID(), instanceData);
                }
            }
        } else if (typeID == MissileData.TYPE_ID) {
            for (MissileData missileData : missiles.values()) {
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
        Set<Short> out = new HashSet<>(deleted);
        deleted.clear();
        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public Map<Byte, Map<Short, InstanceData>> getProjectilesRegistered() {
        Map<Byte, Map<Short, InstanceData>> out = new HashMap<>();

        Map<Short, InstanceData> r = new HashMap<>();
        out.put(RayProjectileData.TYPE_ID, r);
        for (short id : movingRays.keySet()) {
            RayProjectileData e = movingRays.get(id);
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
        for (short id : ballisticProjectiles.keySet()) {
            BallisticProjectileData e = ballisticProjectiles.get(id);
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
        for (short id : missiles.keySet()) {
            MissileData e = missiles.get(id);
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
