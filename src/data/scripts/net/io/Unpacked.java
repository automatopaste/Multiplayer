package data.scripts.net.io;

import data.scripts.net.data.packables.APackable;

import java.util.Map;

public class Unpacked {
    private final Map<Integer, APackable> unpacked;
    private final int tick;

    public Unpacked(Map<Integer, APackable> unpacked, int tick) {
        this.unpacked = unpacked;
        this.tick = tick;
    }

    public Map<Integer, APackable> getUnpacked() {
        return unpacked;
    }

    public int getTick() {
        return tick;
    }
}
