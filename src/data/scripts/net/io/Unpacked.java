package data.scripts.net.io;

import data.scripts.net.data.InboundData;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Unpacked {
    private final InboundData unpacked;
    private final int tick;

    private final InetSocketAddress sender;
    private final InetSocketAddress recipient;
    private final int connectionID;
    private final int size;

    public Unpacked(ByteBuf data, InetSocketAddress sender, InetSocketAddress recipient) {
        this.sender = sender;
        this.recipient = recipient;

        size = data.readableBytes();

        tick = data.readInt();
        connectionID = data.readInt();

        InboundData m;
        try {
            m = BaseConnectionWrapper.readBuffer(data);
        } catch (IOException | DecoderException i) {
            System.err.println("Decode failed for buffer with size " + size + " from " + connectionID);
            i.printStackTrace();
            m = new InboundData(new HashMap<Byte, Map<Short, Map<Byte, Object>>>(), new HashMap<Byte, Set<Short>>());
        }

        unpacked = m;
    }

    public InboundData getUnpacked() {
        return unpacked;
    }

    public int getTick() {
        return tick;
    }

    public InetSocketAddress getRecipient() {
        return recipient;
    }

    public InetSocketAddress getSender() {
        return sender;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public int getSize() {
        return size;
    }
}
