package data.scripts.net.data.tables.client;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.ProjectileTable;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ClientProjectileTable extends EntityTable<ProjectileData> implements InboundEntityManager {

    public ClientProjectileTable() {
        super(new ProjectileData[ProjectileTable.MAX_PROJECTILES]);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        for (ProjectileData data : table) {
            if (data != null) {
                data.update(amount, this);
                data.interp(amount);
            }
        }
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick) {
        if (typeID == ProjectileData.TYPE_ID) {
            ProjectileData data = table[instanceID];

            if (data == null) {
                data = new ProjectileData(instanceID, null, (short) -1, null);
                table[instanceID] = data;

                data.destExecute(toProcess, tick);

                data.init(plugin, this);
            } else {
                data.destExecute(toProcess, tick);
            }
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick) {
        ProjectileData data = table[instanceID];

        if (data != null) {
            data.delete();

            table[instanceID] = null;
            markVacant(instanceID);
        }
    }


    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ProjectileData.TYPE_ID, this);
    }
}
