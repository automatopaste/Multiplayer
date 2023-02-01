package data.scripts.net.data.tables.server;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectileTable extends EntityTable<ProjectileData> implements OutboundEntityManager {

    public static final int MAX_PROJECTILES = 1000;

    private final Set<DamagingProjectileAPI> tracked = new HashSet<>();

    public ProjectileTable() {
        super(new ProjectileData[MAX_PROJECTILES]);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {

    }

    @Override
    public void register() {

    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return null;
    }
}
