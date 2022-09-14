package data.scripts.net.data.packables.entities.variant;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.DestPackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class VariantDest extends DestPackable {

    public VariantDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
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

    @Override
    public int getTypeId() {
        return VariantIDs.TYPE_ID;
    }

    public static VariantDest getDefault() {
        return new VariantDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }

}
