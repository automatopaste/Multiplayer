package data.scripts.net.io;

import data.scripts.net.data.records.ARecord;

import java.util.List;

public class Unpacked {
    private final List<List<ARecord>> unpacked;
    private final int tick;

    public Unpacked(List<List<ARecord>> unpacked, int tick) {
        this.unpacked = unpacked;
        this.tick = tick;
    }

    public List<List<ARecord>> getUnpacked() {
        return unpacked;
    }

    public int getTick() {
        return tick;
    }
}
