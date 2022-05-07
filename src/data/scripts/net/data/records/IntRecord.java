package data.scripts.net.data.records;

import data.scripts.net.data.DataManager;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class IntRecord extends ARecord<Integer> {
    private static final int typeID;
    static {
        typeID = DataManager.registerRecordType(IntRecord.class, new IntRecord(null));
    }

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
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        output.putInt(record);
    }

    @Override
    public IntRecord read(ByteBuf input) {
        int value = input.readInt();
        return new IntRecord(value);
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
