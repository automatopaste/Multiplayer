package data.scripts.net.io;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MessageContainer {

    private final int tick;
    private final ByteBuf data;
    private final InetSocketAddress dest;
    private final byte connectionID;

    public MessageContainer(ByteBuf data, int tick, InetSocketAddress dest, byte connectionID) throws IOException {
        this.tick = tick;
        this.data = data;
        this.dest = dest;
        this.connectionID = connectionID;
    }

    public boolean isEmpty() {
        return data.readableBytes() <= 8;
    }

    public ByteBuf getData() {
        return data;
    }

    public InetSocketAddress getDest() {
        return dest;
    }

    public byte getConnectionID() {
        return connectionID;
    }

    public int getTick() {
        return tick;
    }
}
