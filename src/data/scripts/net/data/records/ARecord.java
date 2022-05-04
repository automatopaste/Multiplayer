package data.scripts.net.data.records;

import data.scripts.net.data.RecordDelta;

import java.nio.ByteBuffer;

public abstract class ARecord implements RecordDelta {
    @Override
    public void write(ByteBuffer output) {
        output.put(getTypeId());
        output.put(getUniqueId());
    }
}
