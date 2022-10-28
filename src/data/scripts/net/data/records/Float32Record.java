package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class Float32Record extends BaseRecord<Float> {
    public static int TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Float32Record(Float record, int uniqueID) {
        super(record, uniqueID);
    }

    public Float32Record(DeltaFunc<Float> deltaFunc, int uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public Float32Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean check() {
        boolean isUpdated;
        Float delta = func.get();

        if (useDecimalPrecision) {
            isUpdated = value.intValue() != delta.intValue();
        } else {
            isUpdated = !value.equals(delta);
        }
        if (isUpdated) value = delta;

        return isUpdated;
    }

    @Override
    public void get(ByteBuf dest) {
        dest.writeFloat(value);
    }

    @Override
    public BaseRecord<Float> read(ByteBuf in, int uniqueID) {
        float value = in.readFloat();
        return new Float32Record(value, uniqueID);
    }

    public static void setTypeId(int typeId) {
        Float32Record.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static BaseRecord<Float> getDefault(int uniqueID) {
        return new Float32Record(0f, uniqueID);
    }

    @Override
    public String toString() {
        return "FloatRecord{" +
                "record=" + value +
                '}';
    }
}
