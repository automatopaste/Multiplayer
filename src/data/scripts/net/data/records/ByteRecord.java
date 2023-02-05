package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class ByteRecord extends DataRecord<Byte> {

    public static byte TYPE_ID;

    public ByteRecord(byte record) {
        super(record);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeByte(value);
    }

    @Override
    public Byte read(ByteBuf in) {
        return in.readByte();
    }

    @Override
    public Byte linterp(float p, Byte v1, Byte v2) {
        int i1 = v1 & 0xFF;
        int i2 = v2 & 0xFF;
        int d = (i2 - i1);
        d += 255;
        return (byte) (Math.round(d * p) + i1 - 255);
    }

    @Override
    public boolean checkUpdate(Byte delta) {
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
