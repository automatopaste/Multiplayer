package data.scripts.net.server;

import data.scripts.net.data.Packable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class ServerSendPacket {
    private static final int PACKET_SIZE_INIT = 1024;

    private final ByteBuffer data;
    private final int length;

    public ServerSendPacket(List<Packable> packables) throws IOException {
        data = ByteBuffer.allocate(PACKET_SIZE_INIT);

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
