package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

public class Vector2f16Record extends BaseRecord<Vector2f> {
    public static byte TYPE_ID;

    public Vector2f16Record(Vector2f record) {
        super(record);
    }

    @Override
    public boolean checkUpdate(Vector2f delta) {
        boolean isUpdated;

        isUpdated = (value.x != delta.x) || (value.y != delta.y);

        return isUpdated;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeShort(ConversionUtils.toHalfFloat(value.x));
        dest.writeShort(ConversionUtils.toHalfFloat(value.y));
    }

    @Override
    public Vector2f read(ByteBuf in) {
        float x = ConversionUtils.toFloat(in.readShort());
        float y = ConversionUtils.toFloat(in.readShort());
        return new Vector2f(x, y);
    }

    @Override
    public Vector2f linterp(float p, Vector2f v1, Vector2f v2) {
        float x = (p * (v2.x - v1.x)) + v1.x;
        float y = (p * (v2.y - v1.y)) + v1.y;
        return new Vector2f(x, y);
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
