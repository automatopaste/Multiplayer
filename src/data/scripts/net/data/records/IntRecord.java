package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class IntRecord extends ARecord<Integer> {
    private int record;

    public IntRecord(int value) {
        record = value;
    }

    public boolean checkUpdate(Integer curr) {
        boolean isUpdated = record != curr;
        if (isUpdated) record = curr;

        return isUpdated;
    }

    public float getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        output.putInt(record);
    }

    public static IntRecord read(ByteBuf input) {
        int value = input.readInt();
        return new IntRecord(value);
    }

    @Override
    public int getTypeId() {
        return IDTypes.INT_RECORD;
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + Integer.toBinaryString(record) +
                '}';
    }
}
