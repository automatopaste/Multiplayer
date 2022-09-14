package data.scripts.net.io;

import data.scripts.net.data.BaseRecord;

import java.net.InetSocketAddress;
import java.util.Map;

public class Unpacked {
    private final Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> unpacked;
    private final int tick;

    private final InetSocketAddress sender;
    private final InetSocketAddress recipient;

    public Unpacked(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> unpacked, int tick, InetSocketAddress sender, InetSocketAddress recipient) {
        this.unpacked = unpacked;
        this.tick = tick;
        this.sender = sender;
        this.recipient = recipient;
    }

    public Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> getUnpacked() {
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
}
