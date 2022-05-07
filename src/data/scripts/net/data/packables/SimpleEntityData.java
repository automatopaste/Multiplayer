package data.scripts.net.data.packables;

import data.scripts.net.data.records.ARecord;
import data.scripts.net.data.records.FloatRecord;

import java.util.Map;

/**
 * Used for decoder debugging
 */
public class SimpleEntityData extends APackable {
    private static int typeID;

    private final FloatRecord info;
//    private final StringRecord string;

    private static final int INFO = 0;

    public SimpleEntityData(int instanceID) {
        super(instanceID);

        info = new FloatRecord(69f);
    }

    public static void setTypeID(int typeID) {
        SimpleEntityData.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    protected boolean write() {
        boolean update = false;
        if (info.checkUpdate(69f)) {
            info.write(packer, INFO);
            update = true;
        }
        return update;
    }

    @Override
    public SimpleEntityData unpack(int instanceID, Map<Integer, ARecord<?>> records) {
        return new SimpleEntityData(instanceID);
    }
}
