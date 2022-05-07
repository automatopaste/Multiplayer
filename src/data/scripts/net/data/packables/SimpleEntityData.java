package data.scripts.net.data.packables;

import data.scripts.net.data.DataManager;
import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.FloatRecord;

import java.util.Map;

/**
 * Used for decoder debugging
 */
public class SimpleEntityData extends APackable {
    private static final int typeID;
    static {
        typeID = DataManager.registerEntityType(SimpleEntityData.class, new SimpleEntityData(-1));
    }

    private final FloatRecord info;
//    private final StringRecord string;

    private static final int INFO = 0;

    public SimpleEntityData(int instanceID) {
        super(instanceID);

        info = new FloatRecord(69f);
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    protected void write() {
        if (info.checkUpdate(69f)) info.write(packer, INFO);
    }

    @Override
    public SimpleEntityData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new SimpleEntityData(instanceID);
    }
}
