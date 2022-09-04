package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.data.DataGenManager;
import data.scripts.net.data.packables.metadata.ConnectionStatusData;
import data.scripts.net.data.packables.trans.InputAggregateData;
import data.scripts.net.data.packables.entities.ShipData;
import data.scripts.net.data.packables.entities.ShipVariantData;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;

public class mpModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        ShipData.setTypeId(DataGenManager.registerEntityType(ShipData.class, new ShipData(-1, (ShipAPI) null)));
        InputAggregateData.setTypeId(DataGenManager.registerEntityType(InputAggregateData.class, new InputAggregateData(-1)));
        ShipVariantData.setTypeId(DataGenManager.registerEntityType(ShipVariantData.class, new ShipVariantData(-1, null, "DEFAULT")));
        ConnectionStatusData.setTypeId(DataGenManager.registerEntityType(ConnectionStatusData.class, new ConnectionStatusData(-1)));

        FloatRecord.setTypeID(DataGenManager.registerRecordType(FloatRecord.class, new FloatRecord(null)));
        IntRecord.setTypeID(DataGenManager.registerRecordType(IntRecord.class, new IntRecord(null)));
        StringRecord.setTypeID(DataGenManager.registerRecordType(StringRecord.class, new StringRecord(null)));
        Vector2fRecord.setTypeID(DataGenManager.registerRecordType(Vector2fRecord.class, new Vector2fRecord(null)));
    }
}