package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class FloatRecord extends ARecord {
    private float record;
    private final int uniqueId;
    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public FloatRecord(float value, int uniqueId) {
        record = value;
        this.uniqueId = uniqueId;
        useDecimalPrecision = true;
    }

    public FloatRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    public boolean update(float curr) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = (int) record != (int) curr;
        } else {
            isUpdated = record != curr;
        }
        if (isUpdated) record = curr;

        return isUpdated;
    }

    public float getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output) {
        super.write(output);

        output.putFloat(record);
    }

    public static FloatRecord read(ByteBuf input) {
        int uniqueId = ARecord.readID(input);

        float value = input.readFloat();
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
