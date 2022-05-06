package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

import java.nio.ByteBuffer;

public class Vector2fRecord extends ARecord<Vector2f> {
    private final Vector2f record;
    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector2fRecord(Vector2f value) {
        record = new Vector2f(value);
        useDecimalPrecision = true;
    }

    public Vector2fRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    public boolean checkUpdate(Vector2f curr) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = (record.x != curr.x) || (record.y != curr.y);
        } else {
            isUpdated = ((int) record.x != (int) curr.x) || ((int) record.y != (int) curr.y);
        }
        if (isUpdated) record.set(curr);

        return true;
        //return isUpdated;
    }

    public Vector2f getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        output.putFloat(record.x);
        output.putFloat(record.y);
    }

    public static Vector2fRecord read(ByteBuf input) {
        float x = input.readFloat();
        float y = input.readFloat();
        return new Vector2fRecord(new Vector2f(x, y));
    }

    @Override
    public int getTypeId() {
        return IDTypes.V2F_RECORD;
    }

    @Override
    public String toString() {
        return "Vector2fRecord{" +
                "record=" + record +
                '}';
    }
}
