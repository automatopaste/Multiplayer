package data.scripts.net.data.records;

import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector3f;

public class Vector3fRecord extends BaseRecord<Vector3f> {

    public static int TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector3fRecord(Vector3f record, int uniqueID) {
        super(record, uniqueID);
    }

    public Vector3fRecord(DeltaFunc<Vector3f> func, int uniqueID) {
        super(func, uniqueID);
    }

    public Vector3fRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public void get(ByteBuf dest) {
        dest.writeFloat(value.x);
        dest.writeFloat(value.y);
        dest.writeFloat(value.z);
    }

    @Override
    public BaseRecord<Vector3f> read(ByteBuf in, int uniqueID) {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        return new Vector3fRecord(new Vector3f(x, y, z), uniqueID);
    }

    @Override
    public BaseRecord<Vector3f> read(ByteArrayReader in, int uniqueID) {
        float x = in.readFloat();
        float y = in.readFloat();
        float z = in.readFloat();
        return new Vector3fRecord(new Vector3f(x, y, z), uniqueID);
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

    public static void setTypeId(int typeId) {
        Vector3fRecord.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static Vector3fRecord getDefault(int uniqueID) {
        return new Vector3fRecord(new Vector3f(0f, 0f, 0f), uniqueID);
    }

    @Override
    public String toString() {
        return "Vector3fRecord{" +
                "value=" + value +
                '}';
    }
}
