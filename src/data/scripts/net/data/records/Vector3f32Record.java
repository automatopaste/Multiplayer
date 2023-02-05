package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector3f;

public class Vector3f32Record extends DataRecord<Vector3f> {

    public static byte TYPE_ID;

    public Vector3f32Record(Vector3f record) {
        super(record);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeFloat(value.x);
        dest.writeFloat(value.y);
        dest.writeFloat(value.z);
    }

    @Override
    public Vector3f read(ByteBuf in) {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        return new Vector3f(x, y, z);
    }

    @Override
    public Vector3f linterp(float p, Vector3f v1, Vector3f v2) {
        float x = (p * (v2.x - v1.x)) + v1.x;
        float y = (p * (v2.y - v1.y)) + v1.y;
        float z = (p * (v2.z - v1.z)) + v1.z;
        return new Vector3f(x, y, z);
    }

    @Override
    public boolean checkUpdate(Vector3f delta) {
        boolean isUpdated;

        isUpdated = (value.x != delta.x) || (value.y != delta.y) || (value.z != delta.z);

        return isUpdated;
    }

    public static void setTypeId(byte typeId) {
        Vector3f32Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static Vector3f32Record getDefault() {
        return new Vector3f32Record(new Vector3f(0f, 0f, 0f));
    }

    @Override
    public String toString() {
        return "Vector3fRecord{" +
                "value=" + value +
                '}';
    }
}
