package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class FloatRecord extends ARecord {
    private float record;
    private final int uniqueId;

    public FloatRecord(float value, int uniqueId) {
        record = value;
        this.uniqueId = uniqueId;
    }

    public boolean update(float curr) {
        boolean isUpdated = record == curr;
        if (isUpdated) record = curr;

        return isUpdated;
    }

    public float getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output) {
        super.write(output);

        output.putInt((int) record);
    }

    public static FloatRecord read(ByteBuf input) {
        byte uniqueId = input.readByte();
        float value = input.readInt();
        return new FloatRecord(value, uniqueId);
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
        return "FloatRecord{" +
                "record=" + record +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
