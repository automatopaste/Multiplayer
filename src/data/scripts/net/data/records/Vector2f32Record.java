package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

public class Vector2f32Record extends BaseRecord<Vector2f> {
    public static int TYPE_ID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector2f32Record(Vector2f record, int uniqueID) {
        super(record, uniqueID);
    }

    public Vector2f32Record(DeltaFunc<Vector2f> deltaFunc, int uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public Vector2f32Record setUseDecimalPrecision(boolean useDecimalPrecision) {
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
    public void get(ByteBuf dest) {
        dest.writeFloat(value.x);
        dest.writeFloat(value.y);
    }

    @Override
    public BaseRecord<Vector2f> read(ByteBuf in, int uniqueID) {
        float x = in.readFloat();
        float y = in.readFloat();
        return new Vector2f32Record(new Vector2f(x, y), uniqueID);
    }

    public static void setTypeId(int typeId) {
        Vector2f32Record.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static Vector2f32Record getDefault(int uniqueID) {
        return new Vector2f32Record(new Vector2f(0f, 0f), uniqueID);
    }

    @Override
    public String toString() {
        return "Vector2fRecord{" +
                "record=" + value +
                '}';
    }
}
