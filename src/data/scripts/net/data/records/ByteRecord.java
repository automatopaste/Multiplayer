package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ByteRecord extends BaseRecord<Byte> {

    public static byte TYPE_ID;

    public ByteRecord(byte record, byte uniqueID) {
        super(record, uniqueID);
    }

    public ByteRecord(DeltaFunc<Byte> func, byte uniqueID) {
        super(func, uniqueID);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeByte(value);
    }

    @Override
    public BaseRecord<Byte> read(ByteBuf in, byte uniqueID) {
        byte value = in.readByte();
        return new ByteRecord(value, uniqueID);
    }

    @Override
    public boolean check() {
        byte delta = func.get();
        boolean isUpdated = value != delta;
        if (isUpdated) value = delta;

        return isUpdated;
    }

    public static ByteRecord getDefault(byte uniqueID) {
        return new ByteRecord((byte) 0, uniqueID);
    }

    public static void setTypeId(byte typeId) {
        ByteRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }
}
