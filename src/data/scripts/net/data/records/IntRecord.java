package data.scripts.net.data.records;

import data.scripts.net.data.BaseRecord;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class IntRecord extends BaseRecord<Integer> {
    private static int typeID;

    public IntRecord(Integer record) {
        super(record);
    }

    @Override
    public boolean checkUpdate(Integer curr) {
        boolean isUpdated = !record.equals(curr);
        if (isUpdated) record = curr;

        return isUpdated;
    }

    @Override
    public void doWrite(ByteBuffer output) {
        output.putInt(record);
    }

    @Override
    public IntRecord read(ByteBuf input) {
        int value = input.readInt();
        return new IntRecord(value);
    }

    public static void setTypeID(int typeID) {
        IntRecord.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + Integer.toBinaryString(record) +
                '}';
    }
}
