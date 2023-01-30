package data.scripts.net.data.tables.server;

import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ProjectileTable extends EntityTable<ProjectileData> implements OutboundEntityManager {

    public ProjectileTable(ProjectileData[] array) {
        super(array);
    }

    @Override
    public void execute(MPPlugin plugin) {

    }

    @Override
    public void update(float amount, MPPlugin plugin) {

    }

    @Override
    public void register() {

    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return null;
    }
}
