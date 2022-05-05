package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class IntRecord extends ARecord {
    private int record;
    private final int uniqueId;

    public IntRecord(int value, int uniqueId) {
        record = value;
        this.uniqueId = uniqueId;
    }

    public boolean update(int curr) {
        boolean isUpdated = record != curr;
        if (isUpdated) record = curr;

        return isUpdated;
    }

    public float getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output) {
        super.write(output);

        output.putInt(record);
    }

    public static IntRecord read(ByteBuf input) {
        int uniqueId = ARecord.readID(input);

        int value = input.readInt();
        return new IntRecord(value, uniqueId);
    }

    @Override
    public int getTypeId() {
        return IDTypes.FLOAT_RECORD;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "IntRecord{" +
                "record=" + record +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
