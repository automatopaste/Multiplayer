package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ByteRecord extends BaseRecord<Byte> {

    public static byte TYPE_ID;

    public ByteRecord(byte record) {
        super(record);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeByte(value);
    }

    @Override
    public BaseRecord<Byte> read(ByteBuf in) {
        byte value = in.readByte();
        return new ByteRecord(value);
    }

    @Override
    public boolean checkNotEqual(Byte delta) {
        return (byte) value != delta;
    }

    public static ByteRecord getDefault() {
        return new ByteRecord((byte) 0);
    }

    public static void setTypeId(byte typeId) {
        ByteRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }
}
