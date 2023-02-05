package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

/**
 * 16 bit float
 */
public class Float16Record extends DataRecord<Float> {

    public static byte TYPE_ID;

    public Float16Record(float record) {
        super(record);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value));
    }

    @Override
    public Float read(ByteBuf in) {
        short value = in.readShort();
        return ConversionUtils.toFloat(value);
    }

    @Override
    public Float linterp(float p, Float v1, Float v2) {
        return (p * (v2 - v1)) + v1;
    }

    @Override
    protected boolean checkUpdate(Float delta) {
        return !value.equals(delta);
    }

    public static DataRecord<Float> getDefault() {
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
