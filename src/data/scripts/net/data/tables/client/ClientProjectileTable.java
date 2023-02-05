package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ClientProjectileTable extends EntityTable<ProjectileData> implements InboundEntityManager {

    public ClientProjectileTable() {
        super(new ProjectileData[100]);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {

    }

    @Override
    public void register() {

    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick) {

    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick) {

    }
}
