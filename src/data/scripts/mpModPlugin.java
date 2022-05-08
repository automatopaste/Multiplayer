package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import data.scripts.net.data.DataManager;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.data.packables.SimpleEntityData;
import data.scripts.net.data.records.FloatRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.Vector2fRecord;

public class mpModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        ShipData.setTypeID(DataManager.registerEntityType(ShipData.class, new ShipData(-1)));
        InputAggregateData.setTypeID(DataManager.registerEntityType(InputAggregateData.class, new InputAggregateData(-1)));
        SimpleEntityData.setTypeID(DataManager.registerEntityType(SimpleEntityData.class, new SimpleEntityData(-1)));

        FloatRecord.setTypeID(DataManager.registerRecordType(FloatRecord.class, new FloatRecord(null)));
        IntRecord.setTypeID(DataManager.registerRecordType(IntRecord.class, new IntRecord(null)));
        StringRecord.setTypeID(DataManager.registerRecordType(StringRecord.class, new StringRecord(null)));
        Vector2fRecord.setTypeID(DataManager.registerRecordType(Vector2fRecord.class, new Vector2fRecord(null)));

        // oi, you got a loicense for unsafe?
        // put this after -classpath in vmparams and install SSMS unlock tool for perf gain
        // ..//mods//unlock.jar;janino.jar;
        System.setProperty("io.netty.noUnsafe", String.valueOf(true));
    }
}