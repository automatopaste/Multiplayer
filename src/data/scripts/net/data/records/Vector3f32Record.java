package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector3f;

public class Vector3f32Record extends BaseRecord<Vector3f> {

    public static byte TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector3f32Record(Vector3f record, byte uniqueID) {
        super(record, uniqueID);
    }

    public Vector3f32Record(DeltaFunc<Vector3f> func, byte uniqueID) {
        super(func, uniqueID);
    }

    public Vector3f32Record setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeFloat(value.x);
        dest.writeFloat(value.y);
        dest.writeFloat(value.z);
    }

    @Override
    public BaseRecord<Vector3f> read(ByteBuf in, byte uniqueID) {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        return new Vector3f32Record(new Vector3f(x, y, z), uniqueID);
    }

    @Override
    public boolean check() {
        boolean isUpdated;
        Vector3f delta = func.get();

        if (useDecimalPrecision) {
            isUpdated = (value.x != delta.x) || (value.y != delta.y) || (value.z != delta.z);
        } else {
            isUpdated = ((int) value.x != (int) delta.x) || ((int) value.y != (int) delta.y) || ((int) value.z != (int) delta.z);
        }
        if (isUpdated) value.set(delta);

        return isUpdated;
    }

    public static void setTypeId(byte typeId) {
        Vector3f32Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static Vector3f32Record getDefault(byte uniqueID) {
        return new Vector3f32Record(new Vector3f(0f, 0f, 0f), uniqueID);
    }

    @Override
    public String toString() {
        return "Vector3fRecord{" +
                "value=" + value +
                '}';
    }
}
