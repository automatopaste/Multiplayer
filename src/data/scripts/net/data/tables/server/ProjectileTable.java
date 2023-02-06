package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.DataGenManager;
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
            if (projectile.isFromMissile()) continue;

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
        table[id] = new ProjectileData(id, projectile, specIDs.get(projectile.getProjectileSpecId()), shipTable);
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
    public Map<Short, Map<Byte, DataRecord<?>>> getOutbound(byte typeID) {
        Map<Short, Map<Byte, DataRecord<?>>> out = new HashMap<>();

        if (typeID == ProjectileData.TYPE_ID) {
            for (int i = 0; i < table.length; i++) {
                ProjectileData data = table[i];
                if (data != null) {
                    Map<Byte, DataRecord<?>> deltas = data.sourceExecute();

                    if (deltas != null && !deltas.isEmpty()) {
                        out.put((short) i, deltas);
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
}
