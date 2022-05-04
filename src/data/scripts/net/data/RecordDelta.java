package data.scripts.net.data;

import java.nio.ByteBuffer;

public interface RecordDelta {

    void write(ByteBuffer output);

    int getTypeId();

    int getUniqueId();
}
