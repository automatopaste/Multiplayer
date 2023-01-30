package data.scripts.net.data.packables.entities.projectiles;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.plugins.MPPlugin;

public class ProjectileData extends BasePackable {

    public ProjectileData(short instanceID) {
        super(instanceID);
    }

    @Override
    public byte getTypeID() {
        return 0;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void init(MPPlugin plugin) {

    }

    @Override
    public void delete() {

    }
}
