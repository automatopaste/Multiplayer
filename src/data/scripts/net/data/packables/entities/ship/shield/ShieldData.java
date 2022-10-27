package data.scripts.net.data.packables.entities.ship.shield;

import com.fs.starfarer.api.combat.ShieldAPI;
import data.scripts.net.data.packables.BasePackable;

public class ShieldData extends BasePackable {

    public ShieldData(int instanceID, ShieldAPI shield) {
        super(instanceID);


    }

    @Override
    public int getTypeID() {
        return 0;
    }
}
