package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class IntRecord extends BaseRecord<Integer> {
    public static byte TYPE_ID;

    public IntRecord(Integer record, byte uniqueID) {
        super(record, uniqueID);
    }

    public IntRecord(DeltaFunc<Integer> deltaFunc, byte uniqueID) {
        super(deltaFunc, uniqueID);
    }

    @Override
    public boolean check() {
        int delta = func.get();
        boolean isUpdated = value != delta;
        if (isUpdated) value = delta;

        return isUpdated;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeInt(value);
    }

    @Override
    public BaseRecord<Integer> read(ByteBuf in, byte uniqueID) {
        int value = in.readInt();
        return new IntRecord(value, uniqueID);
    }

    public static void setTypeId(byte typeId) {
        IntRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static IntRecord getDefault(byte uniqueID) {
        return new IntRecord(0, uniqueID);
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + value +
                '}';
    }
}
