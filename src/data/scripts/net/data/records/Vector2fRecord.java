package data.scripts.net.data.records;

import data.scripts.net.data.BaseRecord;
import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

import java.nio.ByteBuffer;

public class Vector2fRecord extends BaseRecord<Vector2f> {
    private static int typeID;

    private boolean useDecimalPrecision; // if the update checker cares about decimal stuff, use to reduce traffic

    public Vector2fRecord(Vector2f record) {
        super(record);
    }

    public Vector2fRecord setUseDecimalPrecision(boolean useDecimalPrecision) {
        this.useDecimalPrecision = useDecimalPrecision;
        return this;
    }

    @Override
    public boolean check(Vector2f curr) {
        boolean isUpdated;

        if (useDecimalPrecision) {
            isUpdated = (record.x != curr.x) || (record.y != curr.y);
        } else {
            isUpdated = ((int) record.x != (int) curr.x) || ((int) record.y != (int) curr.y);
        }
        if (isUpdated) record.set(curr);

        return isUpdated;
    }

    @Override
    public void doWrite(ByteBuffer output) {
        output.putFloat(record.x);
        output.putFloat(record.y);
    }

    @Override
    public Vector2fRecord read(ByteBuf input) {
        float x = input.readFloat();
        float y = input.readFloat();
        return new Vector2fRecord(new Vector2f(x, y));
    }

    public static void setTypeID(int typeID) {
        Vector2fRecord.typeID = typeID;
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    public String toString() {
        return "Vector2fRecord{" +
                "record=" + record +
                '}';
    }
}
