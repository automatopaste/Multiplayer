package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.*;

public class ProjectileTable extends EntityTable<ProjectileData> implements OutboundEntityManager {

    public static final int MAX_PROJECTILES = 2000;

    private final Map<DamagingProjectileAPI, Short> registered = new HashMap<>();
    private final Map<String, Short> specIDs;
    private final ShipTable shipTable;

    public ProjectileTable(Map<String, Short> specIDs, ShipTable shipTable) {
        super(new ProjectileData[MAX_PROJECTILES]);
        this.specIDs = specIDs;
        this.shipTable = shipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Set<DamagingProjectileAPI> diff = new HashSet<>(registered.keySet());

        for (DamagingProjectileAPI projectile : engine.getProjectiles()) {
            if (projectile instanceof MissileAPI) continue;

            if (registered.containsKey(projectile)) {
                diff.remove(projectile);
            } else {
                createEntry(projectile);
            }
        }

        for (DamagingProjectileAPI d : diff) {
            deleteEntry(d);
        }

        for (ProjectileData projectileData : table) {
            if (projectileData != null) projectileData.update(amount, this);
        }
    }

    private void createEntry(DamagingProjectileAPI projectile) {
        short id = (short) getVacant();
        registered.put(projectile, id);
        try {
            short s = specIDs.get(projectile.getProjectileSpecId());
            table[id] = new ProjectileData(id, projectile, s, shipTable);
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
    }

    private void deleteEntry(DamagingProjectileAPI projectile) {
        short index = registered.get(projectile);
        table[index] = null;
        registered.remove(projectile);
        markVacant(index);
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ProjectileData.TYPE_ID, this);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        if (typeID == ProjectileData.TYPE_ID) {
            for (int i = 0; i < table.length; i++) {
                ProjectileData data = table[i];
                if (data != null) {
                    InstanceData instanceData = data.sourceExecute(amount);

                    if (instanceData.records != null && !instanceData.records.isEmpty()) {
                        out.put((short) i, instanceData);
                    }
                }
            }
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public Map<Short, InstanceData> getProjectilesRegistered() {
        Map<Short, InstanceData> out = new HashMap<>();

        for (short id : registered.values()) {
            ProjectileData projectileData = table[id];

            projectileData.sourceExecute(0f);

            Map<Byte, DataRecord<?>> records = new HashMap<>();
            int size = 0;
            List<RecordLambda<?>> recordLambdas = projectileData.getRecords();
            for (byte i = 0; i < recordLambdas.size(); i++) {
                RecordLambda<?> recordLambda = recordLambdas.get(i);
                records.put(i, recordLambda.record);
                size += recordLambda.record.size();
            }

            out.put(id, new InstanceData(size, records));
        }

        return out;
    }
}
