package data.scripts.net.io;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Unpacked {
    private final Map<Byte, Map<Short, Map<Byte, Object>>> unpacked;
    private final int tick;

    private final InetSocketAddress sender;
    private final InetSocketAddress recipient;
    private final int connectionID;

    public Unpacked(ByteBuf data, InetSocketAddress sender, InetSocketAddress recipient) {
        this.sender = sender;
        this.recipient = recipient;

        tick = data.readInt();
        connectionID = data.readInt();

        Map<Byte, Map<Short, Map<Byte, Object>>> m;
        try {
            m = BaseConnectionWrapper.readBuffer(data);
        } catch (IOException | DecoderException i) {
            i.printStackTrace();
            m = new HashMap<>();
        }

        unpacked = m;
    }

    public Map<Byte, Map<Short, Map<Byte, Object>>> getUnpacked() {
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
}
