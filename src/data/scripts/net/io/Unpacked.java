package data.scripts.net.io;

import data.scripts.net.data.InboundData;
import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;

public class Unpacked {
    private final InboundData unpacked;
    private final int tick;

    private final InetSocketAddress sender;
    private final InetSocketAddress recipient;
    private final byte connectionID;
    private final int latency;
    private final int size;

    public Unpacked(ByteBuf data, InetSocketAddress sender, InetSocketAddress recipient, int latency) {
        this.sender = sender;
        this.recipient = recipient;

        size = data.readableBytes();

        tick = data.readInt();
        connectionID = data.readByte();
        this.latency = latency;

        InboundData m;
        try {
            m = BaseConnectionWrapper.readBuffer(data);
        } catch (Exception e) {
            System.err.println("Decode failed for buffer with size " + size + " from " + connectionID);
            e.printStackTrace();
            m = new InboundData();
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

    public byte getConnectionID() {
        return connectionID;
    }

    public int getSize() {
        return size;
    }

    public int getLatency() {
        return latency;
    }
}
