package data.scripts.net.data.packables;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.FloatRecord;

public class SimpleEntity extends APackable {
    private final FloatRecord info;

    public SimpleEntity() {
        info = new FloatRecord(69f, 1);
    }

    @Override
    public int getTypeId() {
        return IDTypes.SIMPLE_ENTITY;
    }

    @Override
    void update() {
        if (info.update(69f)) info.write(packer);
    }
}