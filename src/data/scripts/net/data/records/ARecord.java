package data.scripts.net.data.records;

import data.scripts.net.data.RecordDelta;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public abstract class ARecord implements RecordDelta {
    @Override
    public void write(ByteBuffer output) {
        output.putInt(getTypeId());
        output.putInt(getUniqueId());
    }

    /**
     * Exists to make it easier to switch to byte or short ids in future
     * @param input ByteBuf to read from
     * @return int id
     */
    public static int readID(ByteBuf input) {
        return input.readInt();
    }
}
