package data.scripts.net.io;

import data.scripts.net.data.Packable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class PacketContainer {
    private static final int PACKET_SIZE_INIT = 1024;

    private final ByteBuffer data;
    private final int length;

    private final int tick;

    public PacketContainer(List<Packable> packables, int tick) throws IOException {
        this.tick = tick;

        data = ByteBuffer.allocate(PACKET_SIZE_INIT);

        data.putInt(tick);

        // number of entities to unpack
        data.putInt(packables.size());

        for (Packable packable : packables) data.put(packable.pack());

        length = data.position();
        data.flip();
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getLength() {
        return length;
    }
}
