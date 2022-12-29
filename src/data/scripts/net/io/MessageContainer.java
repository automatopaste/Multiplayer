package data.scripts.net.io;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MessageContainer {

    private final int tick;
    private final ByteBuf data;
    private final boolean flush;
    private final InetSocketAddress dest;
    private final ByteBuf output;
    private final int connectionID;
    private int bufSize;

    public MessageContainer(ByteBuf data, int tick, boolean flush, InetSocketAddress dest, ByteBuf output, int connectionID) throws IOException {
        this.tick = tick;
        this.data = data;
        this.flush = flush;
        this.dest = dest;
        this.output = output;
        this.connectionID = connectionID;
    }

    public boolean isEmpty() {
        return data.readableBytes() <= 8;
    }

    public ByteBuf getData() {
        return data;
    }

//    public ByteBuf get() {
//        // prevent ref count exception
//        output.retain();
//
//        output.clear();
//
//        // write data
//        output.writeInt(tick);
//        output.writeInt(connectionID);
//
//        for (BasePackable packable : packables) {
//            packable.write(flush, output);
//        }
//
//        bufSize = output.writerIndex();
//
//        return output;
//    }

    public InetSocketAddress getDest() {
        return dest;
    }

    public int getTick() {
        return tick;
    }

    public int getBufSize() {
        return bufSize;
    }

    public int getConnectionID() {
        return connectionID;
    }
}
