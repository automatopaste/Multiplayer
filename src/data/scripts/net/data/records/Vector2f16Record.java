package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

public class Vector2f16Record extends BaseRecord<Vector2f> {
    public static byte TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector2f16Record(Vector2f record) {
        super(record);
    }

    public Vector2f16Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean checkNotEqual(Vector2f delta) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = (value.x != delta.x) || (value.y != delta.y);
        } else {
            isUpdated = ((int) value.x != (int) delta.x) || ((int) value.y != (int) delta.y);
        }

        return isUpdated;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value.x));
        dest.writeShort(ConversionUtils.toHalfFloat(value.y));
    }

    @Override
    public BaseRecord<Vector2f> read(ByteBuf in) {
        float x = ConversionUtils.toFloat(in.readShort());
        float y = ConversionUtils.toFloat(in.readShort());
        return new Vector2f16Record(new Vector2f(x, y));
    }

    public static void setTypeId(byte typeId) {
        Vector2f16Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static Vector2f16Record getDefault() {
        return new Vector2f16Record(new Vector2f(0f, 0f));
    }
}
