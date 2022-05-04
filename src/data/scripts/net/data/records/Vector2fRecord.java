package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;
import org.lwjgl.util.vector.Vector2f;

import java.nio.ByteBuffer;

public class Vector2fRecord extends ARecord {
    private Vector2f record;
    private final int uniqueId;

    public Vector2fRecord(Vector2f value, int uniqueId) {
        record = value;
        this.uniqueId = uniqueId;
    }

    public boolean update(Vector2f curr) {
        boolean isUpdated = record == curr;
        if (isUpdated) record = curr;

        return isUpdated;
    }

    public Vector2f getRecord() {
        return record;
    }

    @Override
    public void write(ByteBuffer output) {
        super.write(output);

        output.putInt((int) record.x);
        output.putInt((int) record.y);
    }

    public static Vector2fRecord read(ByteBuf input) {
        byte uniqueId = input.readByte();
        float x = input.readInt();
        float y = input.readInt();
        return new Vector2fRecord(new Vector2f(x, y), uniqueId);
    }

    @Override
    public int getTypeId() {
        return IDTypes.V2F_RECORD;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "Vector2fRecord{" +
                "record=" + record +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
