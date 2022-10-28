package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ShortRecord extends BaseRecord<Short> {
    public static byte TYPE_ID;

    public ShortRecord(Short record, byte uniqueID) {
        super(record, uniqueID);
    }

    public ShortRecord(DeltaFunc<Short> deltaFunc, byte uniqueID) {
        super(deltaFunc, uniqueID);
    }

    @Override
    public boolean check() {
        short delta = func.get();
        boolean isUpdated = value != delta;
        if (isUpdated) value = delta;

        return isUpdated;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(value);
    }

    @Override
    public BaseRecord<Short> read(ByteBuf in, byte uniqueID) {
        short value = in.readShort();
        return new ShortRecord(value, uniqueID);
    }

    public static void setTypeId(byte typeId) {
        ShortRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static ShortRecord getDefault(byte uniqueID) {
        return new ShortRecord((short) 0, uniqueID);
    }
}
