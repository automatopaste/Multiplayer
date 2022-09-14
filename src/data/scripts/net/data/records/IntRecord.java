package data.scripts.net.data.records;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;

public class IntRecord extends BaseRecord<Integer> {
    public static int TYPE_ID;

    public IntRecord(Integer record, int uniqueID) {
        super(record, uniqueID);
    }

    public IntRecord(DeltaFunc<Integer> deltaFunc, int uniqueID) {
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
    public void get(ByteBuf dest) {
        dest.writeInt(value);
    }

    @Override
    public BaseRecord<Integer> read(ByteBuf in, int uniqueID) {
        int value = in.readInt();
        return new IntRecord(value, uniqueID);
    }

    @Override
    public BaseRecord<Integer> read(ByteArrayReader in, int uniqueID) {
        int value = in.readInt();
        return new IntRecord(value, uniqueID);
    }

    public static void setTypeId(int typeId) {
        IntRecord.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static IntRecord getDefault(int uniqueID) {
        return new IntRecord(0, uniqueID);
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + value +
                '}';
    }
}
