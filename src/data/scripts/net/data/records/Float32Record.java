package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

public class Float32Record extends BaseRecord<Float> {
    public static byte TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Float32Record(Float record) {
        super(record);
    }

    public Float32Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    protected boolean checkNotEqual(Float delta) {
        return (useDecimalPrecision) ? value.intValue() != delta.intValue() : !value.equals(delta);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeFloat(value);
    }

    @Override
    public BaseRecord<Float> read(ByteBuf in) {
        float value = in.readFloat();
        return new Float32Record(value);
    }

    public static void setTypeId(byte typeId) {
        Float32Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static BaseRecord<Float> getDefault() {
        return new Float32Record(0f);
    }
}
