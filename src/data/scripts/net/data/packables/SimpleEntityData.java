package data.scripts.net.data.packables;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.FloatRecord;

/**
 * Used for decoder debugging
 */
public class SimpleEntityData extends APackable {
    private final FloatRecord info;
//    private final StringRecord string;

    private static final int INFO = 0;

    public SimpleEntityData() {
        info = new FloatRecord(69f);

    }

    @Override
    public int getTypeId() {
        return IDTypes.SIMPLE_ENTITY;
    }

    @Override
    void write() {
        if (info.checkUpdate(69f)) info.write(packer, INFO);
    }
}
