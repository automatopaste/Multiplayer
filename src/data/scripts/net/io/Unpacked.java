package data.scripts.net.io;

import data.scripts.net.data.RecordDelta;

import java.util.List;
import java.util.Map;

public class Unpacked {
    private final List<Map<Integer, RecordDelta>> unpacked;
    private final int tick;

    public Unpacked(List<Map<Integer, RecordDelta>> unpacked, int tick) {
        this.unpacked = unpacked;
        this.tick = tick;
    }

    public List<Map<Integer, RecordDelta>> getUnpacked() {
        return unpacked;
    }

    public int getTick() {
        return tick;
    }
}
