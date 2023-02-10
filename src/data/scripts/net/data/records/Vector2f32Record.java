package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

public class Vector2f32Record extends DataRecord<Vector2f> {
    public static byte TYPE_ID;

    public Vector2f32Record(Vector2f record) {
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
        dest.writeFloat(value.x);
        dest.writeFloat(value.y);
    }

    @Override
    public Vector2f read(ByteBuf in) {
        float x = in.readFloat();
        float y = in.readFloat();
        return new Vector2f(x, y);
    }

    @Override
    public Vector2f linterp(float p, Vector2f v1, Vector2f v2) {
        float x = (p * (v2.x - v1.x)) + v1.x;
        float y = (p * (v2.y - v1.y)) + v1.y;
        return new Vector2f(x, y);
    }

    public static void setTypeId(byte typeId) {
        Vector2f32Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static Vector2f32Record getDefault() {
        return new Vector2f32Record(new Vector2f(0f, 0f));
    }

    @Override
    public String toString() {
        return "Vector2fRecord{" +
                "record=" + value +
                '}';
    }

    @Override
    public int size() {
        return 8;
    }
}
