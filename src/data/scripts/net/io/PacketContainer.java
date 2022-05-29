package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketContainer {
    private static final int PACKET_SIZE = 1024;

    private final int tick;
    private final ByteBuf data;

//    private final Queue<ByteBuf> sections;

    public PacketContainer(List<BasePackable> packables, int tick, boolean flush) throws IOException {
        this.tick = tick;

        List<byte[]> entities = new ArrayList<>();
        for (BasePackable packable : packables) {
            byte[] written = packable.pack(flush);
            if (written != null && written.length > 0) entities.add(written);
        }

        data = Unpooled.directBuffer(PACKET_SIZE);

        for (byte[] entity : entities) {
            data.writeBytes(entity);
        }
    }

    public ByteBuf get() {
        return data;
    }

    public int getTick() {
        return tick;
    }
}
