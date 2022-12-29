package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class IntRecord extends BaseRecord<Integer> {
    public static byte TYPE_ID;

    public IntRecord(Integer record) {
        super(record);
    }

    @Override
    protected boolean checkNotEqual(Integer delta) {
        return value != (int) delta;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeInt(value);
    }

    @Override
    public BaseRecord<Integer> read(ByteBuf in) {
        int value = in.readInt();
        return new IntRecord(value);
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
