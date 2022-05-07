package data.scripts.net.data.records;

import data.scripts.net.data.DataManager;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class FloatRecord extends ARecord<Float> {
    private static final int typeID;
    static {
        typeID = DataManager.registerRecordType(FloatRecord.class, new FloatRecord(null));
    }

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
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        output.putFloat(record);
    }

    @Override
    public FloatRecord read(ByteBuf input) {
        float value = input.readFloat();
        return new FloatRecord(value);
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
