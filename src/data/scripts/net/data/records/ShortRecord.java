package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ShortRecord extends DataRecord<Short> {
    public static byte TYPE_ID;

    public ShortRecord(Short record) {
        super(record);
    }

    @Override
    public boolean checkUpdate(Short delta) {
        return value != (short) delta;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(value);
    }

    @Override
    public Short read(ByteBuf in) {
        return in.readShort();
    }

    @Override
    public Short linterp(float p, Short v1, Short v2) {
        return (short) ((p * (v2 - v1)) + v1);
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
