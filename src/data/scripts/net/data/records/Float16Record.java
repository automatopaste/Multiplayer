package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

/**
 * 16 bit float
 */
public class Float16Record extends BaseRecord<Float> {

    public static int TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Float16Record(float record, int uniqueID) {
        super(record, uniqueID);
    }

    public Float16Record(DeltaFunc<Float> deltaFunc, int uniqueID) {
        super(deltaFunc, uniqueID);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value));
    }

    @Override
    public BaseRecord<Float> read(ByteBuf in, int uniqueID) {
        short value = in.readShort();
        return new Float16Record(ConversionUtils.toFloat(value), uniqueID);
    }

    public Float16Record setUseDecimalPrecision(boolean useDecimalPrecision) {
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

    public static BaseRecord<Float> getDefault(int uniqueID) {
        return new Float16Record(0f, uniqueID);
    }

    public static void setTypeId(int typeId) {
        Float16Record.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }
}
