package data.scripts.net.data.packables;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.StringRecord;

public class SimpleEntity extends APackable {
    private final StringRecord info;

    public SimpleEntity() {
        info = new StringRecord("HELP", 1);
    }

    @Override
    public int getTypeId() {
        return IDTypes.SIMPLE_ENTITY;
    }

    @Override
    void update() {
        if (info.update("HELP")) info.write(packer);
    }
}
