package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ShortRecord extends BaseRecord<Short> {
    public static byte TYPE_ID;

    public ShortRecord(Short record) {
        super(record);
    }

    @Override
    public boolean checkNotEqual(Short delta) {
        return value != (short) delta;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(value);
    }

    @Override
    public BaseRecord<Short> read(ByteBuf in) {
        short value = in.readShort();
        return new ShortRecord(value);
    }

    public static void setTypeId(byte typeId) {
        ShortRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static ShortRecord getDefault() {
        return new ShortRecord((short) 0);
    }
}
