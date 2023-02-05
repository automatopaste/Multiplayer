package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class IntRecord extends DataRecord<Integer> {
    public static byte TYPE_ID;

    public IntRecord(Integer record) {
        super(record);
    }

    @Override
    protected boolean checkUpdate(Integer delta) {
        return value != (int) delta;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeInt(value);
    }

    @Override
    public Integer read(ByteBuf in) {
        return in.readInt();
    }

    @Override
    public Integer linterp(float p, Integer v1, Integer v2) {
        return (int) ((v2 - v1) * p) + v1;
    }

    public static void setTypeId(byte typeId) {
        IntRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static IntRecord getDefault() {
        return new IntRecord(0);
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + value +
                '}';
    }
}
