package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import data.scripts.data.DataGenManager;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.data.loading.ShipVariantData;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;

public class mpModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        ShipData.setTypeID(DataGenManager.registerEntityType(ShipData.class, new ShipData(-1)));
        InputAggregateData.setTypeID(DataGenManager.registerEntityType(InputAggregateData.class, new InputAggregateData(-1)));
        ShipVariantData.setTypeID(DataGenManager.registerEntityType(ShipVariantData.class, new ShipVariantData(-1, null, "DEFAULT")));

        FloatRecord.setTypeID(DataGenManager.registerRecordType(FloatRecord.class, new FloatRecord(null)));
        IntRecord.setTypeID(DataGenManager.registerRecordType(IntRecord.class, new IntRecord(null)));
        StringRecord.setTypeID(DataGenManager.registerRecordType(StringRecord.class, new StringRecord(null)));
        Vector2fRecord.setTypeID(DataGenManager.registerRecordType(Vector2fRecord.class, new Vector2fRecord(null)));
    }
}