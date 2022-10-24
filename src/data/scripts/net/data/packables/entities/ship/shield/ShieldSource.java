package data.scripts.net.data.packables.entities.ship.shield;

import com.fs.starfarer.api.combat.ShieldAPI;
import data.scripts.net.data.SourcePackable;

public class ShieldSource extends SourcePackable {

    public ShieldSource(int instanceID, ShieldAPI shield) {
        super(instanceID);


    }

    @Override
    public int getTypeId() {
        return 0;
    }
}
