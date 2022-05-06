package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class FloatRecord extends ARecord<Float> {
    private float record;
    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public FloatRecord(float value) {
        record = value;
        useDecimalPrecision = true;
    }

    public FloatRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean checkUpdate(Float curr) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = (int) record != curr.intValue();
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
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        output.putFloat(record);
    }

    public static FloatRecord read(ByteBuf input) {
        float value = input.readFloat();
        return new FloatRecord(value);
    }

    @Override
    public int getTypeId() {
        return IDTypes.FLOAT_RECORD;
    }

    @Override
    public String toString() {
        return "FloatRecord{" +
                "record=" + record +
                '}';
    }
}
