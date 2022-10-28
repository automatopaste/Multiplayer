package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ByteRecord extends BaseRecord<Byte> {

    public static int TYPE_ID;

    public ByteRecord(byte record, int uniqueID) {
        super(record, uniqueID);
    }

    public ByteRecord(DeltaFunc<Byte> func, int uniqueID) {
        super(func, uniqueID);
    }

    @Override
    public void get(ByteBuf dest) {
        dest.writeByte(value);
    }

    @Override
    public BaseRecord<Byte> read(ByteBuf in, int uniqueID) {
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

    public static ByteRecord getDefault(int uniqueID) {
        return new ByteRecord((byte) 0x00, uniqueID);
    }

    public static void setTypeId(int typeId) {
        ByteRecord.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }
}
