package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

/**
 * 16 bit float
 */
public class Float16Record extends BaseRecord<Float> {

    public static byte TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Float16Record(float record) {
        super(record);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value));
//        dest.writeFloat(value);
    }

    @Override
    public BaseRecord<Float> read(ByteBuf in) {
        short value = in.readShort();
        return new Float16Record(ConversionUtils.toFloat(value));
//        return new Float16Record(in.readFloat());
    }

    public Float16Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    protected boolean checkNotEqual(Float delta) {
        return (useDecimalPrecision) ? value.intValue() != delta.intValue() : !value.equals(delta);
    }

    public static BaseRecord<Float> getDefault() {
        return new Float16Record(0f);
    }

    public static void setTypeId(byte typeId) {
        Float16Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }
}
