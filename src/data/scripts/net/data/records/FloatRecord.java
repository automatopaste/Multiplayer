package data.scripts.net.data.records;

import data.scripts.net.data.BaseRecord;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class FloatRecord extends BaseRecord<Float> {
    private static int typeID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public FloatRecord(Float record) {
        super(record);
    }

    public FloatRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean checkUpdate(Float curr) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = record.intValue() != curr.intValue();
        } else {
            isUpdated = !record.equals(curr);
        }
        if (isUpdated) record = curr;

        return isUpdated;
    }

    @Override
    public void doWrite(ByteBuffer output) {
        output.putFloat(record);
    }

    @Override
    public FloatRecord read(ByteBuf input) {
        float value = input.readFloat();
        return new FloatRecord(value);
    }

    public static void setTypeID(int typeID) {
        FloatRecord.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    public String toString() {
        return "FloatRecord{" +
                "record=" + record +
                '}';
    }
}
