package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import io.netty.buffer.ByteBuf;

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
    private final ByteBuf output;
    private int bufSize;

    public PacketContainer(List<BasePackable> packables, int tick, boolean flush, InetSocketAddress dest, ByteBuf output) throws IOException {
        this.tick = tick;
        this.packables = packables;
        this.flush = flush;
        this.dest = dest;
        this.output = output;
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

        //ByteBuf output = Unpooled.directBuffer(PACKET_SIZE);

        output.writeInt(tick);

        for (byte[] entity : entities) {
            output.writeBytes(entity);
        }

        bufSize = output.writerIndex();

        return output;
    }

    public InetSocketAddress getDest() {
        return dest;
    }

    public int getTick() {
        return tick;
    }

    public int getBufSize() {
        return bufSize;
    }
}
