package data.scripts.net.data.packables.entities.projectiles;

import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

public class MissileData extends EntityData {
    public MissileData(short instanceID) {
        super(instanceID);
    }

    @Override
    public byte getTypeID() {
        return 0;
    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void delete() {

    }
}
