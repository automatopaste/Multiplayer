package data.scripts.net.data;

import java.nio.ByteBuffer;

public interface RecordDelta {

    void write(ByteBuffer output);

    byte getTypeId();

    byte getUniqueId();
}
