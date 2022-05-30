package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PacketContainer {
    private static final int PACKET_SIZE = 1024;

    private final int tick;
    private final List<BasePackable> packables;
    private final boolean flush;
    private final InetSocketAddress dest;

//    private final Queue<ByteBuf> sections;

    public PacketContainer(List<BasePackable> packables, int tick, boolean flush, InetSocketAddress dest) throws IOException {
        this.tick = tick;
        this.packables = packables;
        this.flush = flush;
        this.dest = dest;
    }

    public void addPackable(BasePackable toAdd) {
        packables.add(toAdd);
    }

    public void addPackables(List<BasePackable> toAdd) {
        packables.addAll(toAdd);
    }

    public boolean isEmpty() {
        return packables.isEmpty();
    }

    public ByteBuf get() {
        List<byte[]> entities = new ArrayList<>();
        for (BasePackable packable : packables) {
            byte[] written = packable.pack(flush);
            if (written != null && written.length > 0) entities.add(written);
        }

        ByteBuf data = Unpooled.directBuffer(PACKET_SIZE);

        for (byte[] entity : entities) {
            data.writeBytes(entity);
        }

        return data;
    }

    public InetSocketAddress getDest() {
        return dest;
    }

    public int getTick() {
        return tick;
    }
}
