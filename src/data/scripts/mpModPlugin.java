package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.packables.metadata.ConnectionStatusData;
import data.scripts.net.data.packables.trans.PilotCommandData;
import data.scripts.net.data.packables.entities.ShipData;
import data.scripts.net.data.packables.entities.VariantData;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;

public class mpModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        ShipData.setTypeId(DataGenManager.registerEntityType(ShipData.class, new ShipData(-1, (ShipAPI) null)));
        PilotCommandData.setTypeId(DataGenManager.registerEntityType(PilotCommandData.class, new PilotCommandData(-1)));
        VariantData.setTypeId(DataGenManager.registerEntityType(VariantData.class, new VariantData(-1, null, "DEFAULT")));
        ConnectionStatusData.setTypeId(DataGenManager.registerEntityType(ConnectionStatusData.class, new ConnectionStatusData(-1)));

        FloatRecord.setTypeID(DataGenManager.registerRecordType(FloatRecord.class, new FloatRecord(null)));
        IntRecord.setTypeID(DataGenManager.registerRecordType(IntRecord.class, new IntRecord(null)));
        StringRecord.setTypeID(DataGenManager.registerRecordType(StringRecord.class, new StringRecord(null)));
        Vector2fRecord.setTypeID(DataGenManager.registerRecordType(Vector2fRecord.class, new Vector2fRecord(null)));
    }
}