package data.scripts.net.io;

import data.scripts.net.data.BasePackable;

import java.util.Map;

public class Unpacked {
    private final Map<Integer, BasePackable> unpacked;
    private final int tick;

    public Unpacked(Map<Integer, BasePackable> unpacked, int tick) {
        this.unpacked = unpacked;
        this.tick = tick;
    }

    public Map<Integer, BasePackable> getUnpacked() {
        return unpacked;
    }

    public int getTick() {
        return tick;
    }
}
