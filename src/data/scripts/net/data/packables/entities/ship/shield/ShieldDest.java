package data.scripts.net.data.packables.entities.ship.shield;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.packables.entities.variant.VariantIDs;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public class ShieldDest extends DestPackable {

    public ShieldDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    public int getTypeID() {
        return 0;
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(IntRecord.getDefault(VariantIDs.CAPACITORS));
        putRecord(IntRecord.getDefault(VariantIDs.VENTS));
        putRecord(ListRecord.getDefault(VariantIDs.WEAPON_IDS, StringRecord.TYPE_ID));
        putRecord(ListRecord.getDefault(VariantIDs.WEAPON_SLOTS, StringRecord.TYPE_ID));
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
