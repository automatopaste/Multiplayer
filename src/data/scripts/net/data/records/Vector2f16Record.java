package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

public class Vector2f16Record extends BaseRecord<Vector2f> {
    public static byte TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector2f16Record(Vector2f record, byte uniqueID) {
        super(record, uniqueID);
    }

    public Vector2f16Record(DeltaFunc<Vector2f> deltaFunc, byte uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public Vector2f16Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean check() {
        boolean isUpdated;
        Vector2f delta = func.get();

        if (useDecimalPrecision) {
            isUpdated = (value.x != delta.x) || (value.y != delta.y);
        } else {
            isUpdated = ((int) value.x != (int) delta.x) || ((int) value.y != (int) delta.y);
        }
        if (isUpdated) value.set(delta);

        return isUpdated;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value.x));
        dest.writeShort(ConversionUtils.toHalfFloat(value.y));
    }

    @Override
    public BaseRecord<Vector2f> read(ByteBuf in, byte uniqueID) {
        float x = ConversionUtils.toFloat(in.readShort());
        float y = ConversionUtils.toFloat(in.readShort());
        return new Vector2f16Record(new Vector2f(x, y), uniqueID);
    }

    public static void setTypeId(byte typeId) {
        Vector2f16Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static Vector2f16Record getDefault(byte uniqueID) {
        return new Vector2f16Record(new Vector2f(0f, 0f), uniqueID);
    }
}
